
#include <cassert>
#include <iostream>

#include "be_vib_imagej_DenoisingIJ2.h"

#include "quasar_dsl.h"

using namespace quasar;

namespace
{
	IQuasarHost* host = nullptr;
}

namespace
{
	QValue QuasarCubeFromJavaByteArray8BitGrayscale(JNIEnv *env, jint width, jint height, jbyteArray pixels)
	{
		// Important note: Quasar cubes use the (y, x, z) indexing convention. See QuickReferenceManual.pdf section 2.1 page 11.

		QValue qvImage = host->CreateCube<scalar>(height, width, 1); // we're assuming pixels represents an 8 bit grayscale image (tested elsewhere)
		Cube cube = host->LockCube<scalar>(qvImage);

		jbyte *values = env->GetByteArrayElements(pixels, nullptr);
		int3 sz = make_int3(height, width, 1);
		for (jint y = 0; y < height; y++)
		{
			for (jint x = 0; x < width; x++)
			{
				jint idx = y * width + x;
				float gray = (float)(values[idx] & 0xff);  // CHECKME: is the mask needed?

				// store gray value in Quasar cube object
				cube.data[pos2ind(sz, make_int3(y, x, 0))] = gray;
			}
		}
		env->ReleaseByteArrayElements(pixels, values, JNI_ABORT);
		host->UnlockCube(qvImage);
		return qvImage;
	}

	jbyteArray QuasarCubeToJavaByteArray8BitGrayscale(const QValue& qvImage, JNIEnv *env, jint width, jint height)
	{
		// See http://electrofriends.com/articles/jni/jni-part-5-jni-arrays/ for info on creating and changing e.g. jintArray

		Cube cube = host->LockCube<scalar>(qvImage);  // TODO: is this needed when just reading the cube data?

		jbyteArray pixels = env->NewByteArray(width * height);

		// FIXME: remove width and height parameters, and get them from the Quasar cube instead

		jbyte *values = env->GetByteArrayElements(pixels, nullptr);
		int3 sz = make_int3(height, width, 1);
		for (jint y = 0; y < height; y++)
		{
			for (jint x = 0; x < width; x++)
			{
				// read gray value from quasar cube object
				// (note: Quasar cubes use (y, x, z) indexing convention)
				float gray = cube.data[pos2ind(sz, make_int3(y, x, 0))];

				// Assumption: gray is in [0, 255]
				unsigned grayint = static_cast<unsigned>(gray);

				jbyte color = (jbyte)grayint;

				jint idx = y * width + x;
				values[idx] = color;
			}
		}
		env->SetByteArrayRegion(pixels, 0, width * height, values);
		env->ReleaseByteArrayElements(pixels, values, JNI_ABORT);

		host->UnlockCube(qvImage);

		return pixels;
	}

	// Apply non-local means filter
	QValue QuasarNonlocalMeans(QValue qvNoisyImage, float sigma, int searchWindow, int halfBlockSize, int vectorBasedFilter, int kltPostProcessing)
	{
		assert(vectorBasedFilter == 0 || vectorBasedFilter == 1);
		assert(kltPostProcessing == 0 || kltPostProcessing == 1);

		const QValue qvSigma(sigma);
		const QValue qvSearchWnd(searchWindow);

		const int hbs[] = { halfBlockSize, halfBlockSize };
		const QValue qvHalfBlockSize = QValue(hbs);

		const QValue qvVectorBasedFilter(vectorBasedFilter);
		const QValue qvKLTPostprocessing(kltPostProcessing);

		Function nlmeans(L"denoise_nlmeans(cube, scalar, int, ivec2, int, int)");

		return nlmeans(qvNoisyImage, qvSigma, qvSearchWnd, qvHalfBlockSize, qvVectorBasedFilter, qvKLTPostprocessing);
	}
} // close anonymous namespace

jboolean Java_be_vib_imagej_DenoisingIJ2_quasarInit(JNIEnv* env, jclass, jstring deviceName)
{
	std::cout << "Java_be_vib_imagej_DenoisingIJ2_quasarInit" << std::endl;

	const jchar* deviceNameStr = env->GetStringChars(deviceName, nullptr);   // Note: jchar is an unsigned 16 bits data type
 	LPCWSTR deviceNameWide = (wchar_t const*)deviceNameStr;

 	host = quasar::IQuasarHost::Create(deviceNameWide, true);

 	env->ReleaseStringChars(deviceName, deviceNameStr);

 	return host ? JNI_TRUE : JNI_FALSE;
}

void Java_be_vib_imagej_DenoisingIJ2_quasarRelease(JNIEnv* env, jclass)
{
	std::cout << "Java_be_vib_imagej_DenoisingIJ2_quasarRelease" << std::endl;
	assert(host);
	host->Release();
	host = nullptr;
}

jboolean Java_be_vib_imagej_DenoisingIJ2_quasarLoadSource(JNIEnv* env, jclass, jstring source)
{
	std::cout << "Java_be_vib_imagej_DenoisingIJ2_quasarLoadSource" << std::endl;
	assert(host);

	const jchar* sourceStr = env->GetStringChars(source, nullptr);   // Note: jchar is an unsigned 16 bits data type
 	LPCWSTR sourceWide = (wchar_t const*)sourceStr;

	LPCWSTR errorMsg;
	bool loaded = host->LoadSourceModule(sourceWide, &errorMsg);
	if (!loaded)
	{
		wprintf(L"LoadSourceModule error: %s\n", errorMsg);
	}
	else
	{
		wprintf(L"Quasar source module %s loaded.\n", sourceWide);
	}

 	env->ReleaseStringChars(source, sourceStr);

 	return loaded ? JNI_TRUE : JNI_FALSE;
}

jbyteArray Java_be_vib_imagej_DenoisingIJ2_quasarNlmeans(JNIEnv *env, jclass, jint width, jint height, jbyteArray inputPixels, jfloat sigma, jint searchWindow, jint halfBlockSize,  jint vectorBasedFilter, jint kltPostProcessing)
{
	std::cout << "Java_be_vib_imagej_DenoisingIJ2_quasarNlmeans" << std::endl;

	QValue qvNoisyImage = QuasarCubeFromJavaByteArray8BitGrayscale(env, width, height, inputPixels);

	QValue qvDenoisedImage = QuasarNonlocalMeans(qvNoisyImage, sigma, searchWindow, halfBlockSize, vectorBasedFilter, kltPostProcessing);

	return QuasarCubeToJavaByteArray8BitGrayscale(qvDenoisedImage, env, width, height);
}

