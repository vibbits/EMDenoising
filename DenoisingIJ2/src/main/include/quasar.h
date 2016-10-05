//=============================================================================
// Quasar CUDA / OpenMP runtime backend
// (C) 2011-2015 Bart Goossens
//=============================================================================
#ifndef QUASAR_H_INCLUDED
#define QUASAR_H_INCLUDED

#ifndef __CUDACC_RTC__
#include <limits.h>
#else
#define SCHAR_MIN	(-128)
#define SCHAR_MAX	127
#define UCHAR_MAX	255
#define CHAR_MIN	0
#define CHAR_MAX	UCHAR_MAX
#define INT_MAX		2147483647
#define INT_MIN		(-INT_MAX-1)
#define UINT_MAX	0xffffffff
#define SHRT_MAX	32767
#define SHRT_MIN	(-SHRT_MAX-1)
#define USHRT_MAX	0xffff
#define LONG_MAX	2147483647L
#define LONG_MIN	(-LONG_MAX-1)
#define ULONG_MAX	0xffffffffUL
#define LLONG_MAX 9223372036854775807LL
#define LLONG_MIN (-LLONG_MAX - 1)
#define ULLONG_MAX (2ULL * LLONG_MAX + 1)
#define FLT_MAX
#define FLT_RADIX 2
#define FLT_MANT_DIG 24
#define FLT_DIG 6
#define FLT_ROUNDS 1
#define FLT_EPSILON 1.1920929e-07F
#define FLT_MIN_EXP (-125)
#define FLT_MIN 1.17549435e-38F
#define FLT_MIN_10_EXP (-37)
#define FLT_MAX_EXP 128
#define FLT_MAX 3.40282347e+38F
#define FLT_MAX_10_EXP 38
#define DBL_MANT_DIG 53
#define DBL_DIG 15
#define DBL_EPSILON 2.2204460492503131e-16
#define DBL_MIN_EXP (-1021)
#define DBL_MIN 2.2250738585072014e-308
#define DBL_MIN_10_EXP (-307)
#define DBL_MAX_EXP 1024
#define DBL_MAX 1.7976931348623157e+308
#define DBL_MAX_10_EXP 308
#endif

// NVCC compiler in device mode...
#if defined(__CUDA_ARCH__) && !defined(TARGET_CUDA)
#define TARGET_CUDA
#endif

#ifdef TARGET_CUDA
#ifndef __CUDACC_RTC__
#include "cuda_runtime.h"
#include <cstdio>
#include <float.h>
#endif
#else
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <float.h>
#ifdef _OPENMP
#include <omp.h>
#endif
#endif
#ifndef __CUDACC_RTC__
#include <new>
#endif

//+++++++ INTEGER TYPES +++++++++
#if defined(_MSC_VER) || defined(__CUDACC_RTC__)
// Visual C++: stdint.h file is missing on the default installation...
typedef signed char int8_t;
typedef unsigned char uint8_t;
typedef signed short int16_t;
typedef unsigned short uint16_t;
typedef signed int int32_t;
typedef unsigned int uint32_t;
typedef signed long long int64_t;
typedef unsigned long long uint64_t;
#else
#include <stdint.h>
#endif

#if defined(WIN64) || defined(_WIN64)
#define __LP64__
#endif

#if defined(__GNUC__) && (defined(__x86_64__) || defined(__ppc64__) || defined(__aarch64__))
#define __LP64__
#endif

#if defined(__LP64__)
#pragma message ("Pointer size is 64-bit.")
#endif


#define VALUE_TO_STRING(x) #x
#define VAR_NAME_VALUE(var) #var "=" VALUE_TO_STRING(var)

#if __cplusplus >= 201103L
#define CPLUSPLUS11
#elif defined(_MSC_VER) && _MSC_VER >= 1800
// Visual C++ 2013 supports C++ 11
#define CPLUSPLUS11
#elif defined(__GNUC__) && __GNUC__ >= 5
// GCC 5.0
#define CPLUSPLUS11
#endif

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define M_MAXLOG 7.09782712893383996732E2
#define M_MINLOG -7.451332191019412076235E2
#define M_MAXGAM 171.624376956302725
#define M_SQTPI 2.50662827463100050242E0
#define M_SQRTH 7.07106781186547524401E-1
#define M_LOGPI 1.14472988584940017414
#define M_LOG2 0.693147180559945309417

#ifndef M_EPS
#ifdef DBL_SCALAR
#define M_EPS 2.2204460492503131e-016 /*DBL_EPSILON*/
#else
#define M_EPS 1.192092896e-07F /*FLT_EPSILON*/
#endif
#endif

// NAN/INFINITY MACROS
#ifndef NAN
// CUDA does not support std::numeric_limits<scalar>
#ifdef TARGET_CUDA
#ifdef DBL_SCALAR
#define INFINITY __longlong_as_double(0x7ff0000000000000ULL)
#define NAN __longlong_as_double(0xfff8000000000000ULL)
#else
#define INFINITY __int_as_float(0x7f800000)
#define NAN __int_as_float(0x7fffffff)
#endif
#else
#include <limits>
#define INFINITY std::numeric_limits<scalar>::infinity()
#define NAN std::numeric_limits<scalar>::quiet_NaN()
#endif
#endif

#define NEG_INFINITY (-INFINITY)

#undef INLINE
#if defined(__CUDACC_RTC__)
#define INLINE __inline
#define EXPORT
#define SELECTANY
#elif defined(__clang__)
#define INLINE inline
#define EXPORT
#define SELECTANY
#elif defined(__GNUG__)
#define INLINE inline
#define EXPORT
#if defined(_WIN32) || defined(_WIN64)
#define SELECTANY __attribute__((selectany))
#else
#define SELECTANY
#endif
#else
#define INLINE __forceinline
#define EXPORT __declspec(dllexport)
#define SELECTANY __declspec(selectany)
#endif

#define ENTRY(x) _##x

#ifdef _MSC_VER
#pragma warning(disable : 4247 4297 4190) // C-linkage related issues throwing
                                          // exceptions from extern "C"
                                          // functions
#pragma warning(disable : 4305 4244)      // Truncation from double to scalar
#endif

//++++++++ MAIN SCALAR DEF ++++++++++
#ifdef DBL_SCALAR
typedef double scalar;
#else
typedef float scalar;
#endif

//+++++++ IFDEF TARGET_CUDA +++++++++
#ifdef TARGET_CUDA

#ifdef DBL_SCALAR
typedef double1 scalar1;
typedef double2 scalar2;
typedef double3 scalar3;
typedef double4 scalar4;
#define make_scalar1(x) make_double1(x)
#define make_scalar2(x, y) make_double2(x, y)
#define make_scalar3(x, y, z) make_double3(x, y, z)
#define make_scalar4(x, y, z, w) make_double4(x, y, z, w)
#else
typedef float1 scalar1;
typedef float2 scalar2;
typedef float3 scalar3;
typedef float4 scalar4;
#define make_scalar1(x) make_float1(x)
#define make_scalar2(x, y) make_float2(x, y)
#define make_scalar3(x, y, z) make_float3(x, y, z)
#define make_scalar4(x, y, z, w) make_float4(x, y, z, w)
#endif

#else

/* NON CUDA TARGETS - do some elementary definitions */

// G++ compiler known _Pragma, not __pragma
#ifdef __GNUG__
#define __pragma(x) _Pragma(#x)
#define __align__(x) __attribute__((packed, aligned(x)))
#else
#define __thread __declspec(thread)
#define __align__(x) __declspec(align(x))
#endif

#define __device__
#define __host__
#define __global__
#define __constant__
#define __shared__
#define __syncthreads() __pragma(omp barrier)
#endif


// Built-in functions
// __threadidx: returns the current thread index
__device__ INLINE int __threadidx(void)
{
#if defined(_OPENMP)
    return omp_get_thread_num();
#elif defined(TARGET_CUDA)
    return (threadIdx.x * blockDim.y + threadIdx.y) * blockDim.z + threadIdx.z;
#else
    return 0;
#endif
}

// __blockidx: returns the current block index
__device__ INLINE int __blockidx(void)
{
#ifdef TARGET_CUDA
    return (blockIdx.x * gridDim.x + blockIdx.y) * gridDim.z + blockIdx.x;
#else
    return 0;
#endif
}

// __threadcnt: returns the current thread count
__device__ INLINE int __threadcnt(void)
{
#if defined(_OPENMP)
    return omp_get_num_threads();
#elif defined(TARGET_CUDA)
    return blockDim.x * blockDim.y * blockDim.z;
#else
    return 1;
#endif
}

// Calculating the next power of two of the specified number
INLINE __host__ __device__ unsigned int NextPow2(unsigned int v)
{
    v--;
    v |= v >> 1;
    v |= v >> 2;
    v |= v >> 4;
    v |= v >> 8;
    v |= v >> 16;
    return ++v;
}

namespace quasar
{

    //=============================================================================
    // SUPPORT FOR VECTOR / MATRIX / CUBE DATA TYPES
    // NOTE: defining the copy constructors results in a segment violation under
    // GCC
    // when parameters are passed in C-style
    //=============================================================================
    template <class T> struct VectorBase
    {
        T *data;
        int dim1;

        INLINE __device__ __host__ VectorBase() : data(NULL) {}
        INLINE __device__ __host__ VectorBase(T *data, int dim1) : data(data), dim1(dim1)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ int get_numel() const { return dim1; }
    };

    template <class T> struct MatrixBase
    {
        T *data;
        int dim1;
        int dim2;

        INLINE __device__ __host__ MatrixBase() : data(NULL) {}
        INLINE __device__ __host__ MatrixBase(T *data, int dim1, int dim2)
            : data(data), dim1(dim1), dim2(dim2)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2; }
    };

    template <class T> struct CubeBase
    {
        T *data;
        int dim1;
        int dim2;
        int dim3;

        INLINE __device__ __host__ CubeBase() : data(NULL) {}
        INLINE __device__ __host__ CubeBase(T *data, int dim1, int dim2, int dim3)
            : data(data), dim1(dim1), dim2(dim2), dim3(dim3)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2 * dim3; }
    };

// Specialized classes (only useful when dynamic memory support is enabled)
#if defined(ENABLE_DYNAMIC_KERNEL_MEM) || defined(DYNMEM2_0)

    template <class T> struct VectorBase<VectorBase<T> >
    {
        VectorBase<T> *data;
        int dim1;

        INLINE __device__ __host__ VectorBase() : data(NULL) {}
        INLINE __device__ __host__ VectorBase(VectorBase<T> *data, int dim1)
            : data(data), dim1(dim1)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1; }
    };

    template <class T> struct VectorBase<MatrixBase<T> >
    {
        MatrixBase<T> *data;
        int dim1;

        INLINE __device__ __host__ VectorBase() : data(NULL) {}
        INLINE __device__ __host__ VectorBase(MatrixBase<T> *data, int dim1)
            : data(data), dim1(dim1)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1; }
    };

    template <class T> struct VectorBase<CubeBase<T> >
    {
        CubeBase<T> *data;
        int dim1;

        INLINE __device__ __host__ VectorBase() : data(NULL) {}
        INLINE __device__ __host__ VectorBase(CubeBase<T> *data, int dim1)
            : data(data), dim1(dim1)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1; }
    };

    template <class T> struct MatrixBase<VectorBase<T> >
    {
        VectorBase<T> *data;
        int dim1;
        int dim2;

        INLINE __device__ __host__ MatrixBase() : data(NULL) {}
        INLINE __device__ __host__ MatrixBase(VectorBase<T> *data, int dim1, int dim2)
            : data(data), dim1(dim1), dim2(dim2)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2; }
    };

    template <class T> struct MatrixBase<MatrixBase<T> >
    {
        MatrixBase<T> *data;
        int dim1;
        int dim2;

        INLINE __device__ __host__ MatrixBase() : data(NULL) {}
        INLINE __device__ __host__ MatrixBase(MatrixBase<T> *data, int dim1, int dim2)
            : data(data), dim1(dim1), dim2(dim2)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2; }
    };

    template <class T> struct MatrixBase<CubeBase<T> >
    {
        CubeBase<T> *data;
        int dim1;
        int dim2;

        INLINE __device__ __host__ MatrixBase() : data(NULL) {}
        INLINE __device__ __host__ MatrixBase(CubeBase<T> *data, int dim1, int dim2)
            : data(data), dim1(dim1), dim2(dim2)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2; }
    };

    template <class T> struct CubeBase<VectorBase<T> >
    {
        VectorBase<T> *data;
        int dim1;
        int dim2;
        int dim3;

        INLINE __device__ __host__ CubeBase() : data(NULL) {}
        INLINE __device__ __host__ 
        CubeBase(VectorBase<T> *data, int dim1, int dim2, int dim3)
            : data(data), dim1(dim1), dim2(dim2), dim3(dim3)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2 * dim3; }
    };

    template <class T> struct CubeBase<MatrixBase<T> >
    {
        MatrixBase<T> *data;
        int dim1;
        int dim2;
        int dim3;

        INLINE __device__ __host__ CubeBase() : data(NULL) {}
        INLINE __device__ __host__ 
        CubeBase(MatrixBase<T> *data, int dim1, int dim2, int dim3)
            : data(data), dim1(dim1), dim2(dim2), dim3(dim3)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2 * dim3; }
    };

    template <class T> struct CubeBase<CubeBase<T> >
    {
        CubeBase<T> *data;
        int dim1;
        int dim2;
        int dim3;

        INLINE __device__ __host__ CubeBase() : data(NULL) {}
        INLINE __device__ __host__ 
        CubeBase(CubeBase<T> *data, int dim1, int dim2, int dim3)
            : data(data), dim1(dim1), dim2(dim2), dim3(dim3)
        {
        }
        INLINE __device__ void addref();
        INLINE __device__ void release();
        INLINE __device__ __host__ int get_numel() const { return dim1 * dim2 * dim3; }
    };

#endif

} // namespace quasar

#if !defined(TARGET_CUDA)

template <class T> struct default_val
{
    static T val;
};

#define C_NULL_REF(type) ((type &)default_val<type>::val)
template <class T> T default_val<T>::val;

#else
#define C_NULL_REF(type) ((type &)*(type *)NULL)
#endif

//=============================================================================
// SUPPORT FOR NON-CUDA TARGETS
//=============================================================================
//+++++++ IFNDEF TARGET_CUDA +++++++++
#ifndef TARGET_CUDA

/* Built-in types */
struct int1
{
    int x;
};
struct int2
{
    int x, y;
};
struct int3
{
    int x, y, z;
};
struct int4
{
    int x, y, z, w;
};
struct scalar1
{
    scalar x;
};
struct scalar2
{
    scalar x, y;
};
struct scalar3
{
    scalar x, y, z;
};
struct scalar4
{
    scalar x, y, z, w;
};

/* Constructors for built-in types */
INLINE int1 make_int1(int x)
{
    int1 t = {x};
    return t;
}
INLINE int2 make_int2(int x, int y)
{
    int2 t = {x, y};
    return t;
}
INLINE int3 make_int3(int x, int y, int z)
{
    int3 t = {x, y, z};
    return t;
}
INLINE int4 make_int4(int x, int y, int z, int w)
{
    int4 t = {x, y, z, w};
    return t;
}
INLINE scalar1 make_scalar1(scalar x)
{
    scalar1 t = {x};
    return t;
}
INLINE scalar2 make_scalar2(scalar x, scalar y)
{
    scalar2 t = {x, y};
    return t;
}
INLINE scalar3 make_scalar3(scalar x, scalar y, scalar z)
{
    scalar3 t = {x, y, z};
    return t;
}
INLINE scalar4 make_scalar4(scalar x, scalar y, scalar z, scalar w)
{
    scalar4 t = {x, y, z, w};
    return t;
}


#define M_REC_LN2 1.4426950408889634073599246810019 /* 1/ln(2) */

/* Evaluates polynomial of degree N */
INLINE scalar __device__ polevl(scalar x, const scalar *coef, int N)
{
    scalar ans = coef[0];
    for (int i = 1; i <= N; i++)
        ans = ans * x + coef[i];
    return ans;
}

/* Evaluates polynomial of degree N with assumtion that coef[N] = 1.0 */
INLINE scalar __device__ p1evl(scalar x, const scalar *coef, int N)
{
    scalar ans = x + coef[0];
    for (int i = 1; i < N; i++)
        ans = ans * x + coef[i];
    return ans;
}

/* Built-in functions - note: we need to overload the standard definitions -
 * otherwise the wrong version could be used (for example abs(int) instead of
 * fabs(float))
 * By including the std namespace, most of these definitions should be included
 */
using namespace std;

// C++ 11: copysign function is already defined!
#ifndef CPLUSPLUS11
INLINE scalar copysign(scalar fTo, scalar fFrom)
{
    scalar t;
    *(unsigned int *)&t = ((*(unsigned int *)&fTo & 0x7FFFFFFF) |
                           (*(unsigned int *)&fFrom & 0x80000000));
    return t;
}

INLINE scalar round(scalar r)
{
    return (r > 0.0) ? floor(r + 0.5) : ceil(r - 0.5);
}

INLINE scalar exp2(scalar x) { return powf(2.0f, x); }
INLINE scalar log2(scalar x) { return log(x) * M_REC_LN2; }
INLINE scalar asinh(scalar xx)
{
    scalar x;
    int sign;
    if (xx == 0)
        return xx;
    if (xx < 0)
    {
        sign = -1;
        x = -xx;
    }
    else
    {
        sign = 1;
        x = xx;
    }
    return sign * log(x + sqrt(x * x + 1));
}
INLINE scalar acosh(scalar x)
{
    if (x < 1.0)
        return NAN;
    return log(x + sqrt(x * x - 1));
}
INLINE scalar atanh(scalar x)
{
    if (x < -1 || x > 1)
        return NAN;
    return 0.5 * log((1.0 + x) / (1.0 - x));
}
INLINE scalar erf(scalar x);
INLINE scalar erfc(scalar a)
{
    scalar x, y, z, p, q;
    scalar P[] = {2.46196981473530512524E-10, 5.64189564831068821977E-1,
                  7.46321056442269912687E0,   4.86371970985681366614E1,
                  1.96520832956077098242E2,   5.26445194995477358631E2,
                  9.34528527171957607540E2,   1.02755188689515710272E3,
                  5.57535335369399327526E2};
    scalar Q[] = {// 1.0
                  1.32281951154744992508E1, 8.67072140885989742329E1,
                  3.54937778887819891062E2, 9.75708501743205489753E2,
                  1.82390916687909736289E3, 2.24633760818710981792E3,
                  1.65666309194161350182E3, 5.57535340817727675546E2};
    scalar R[] = {5.64189583547755073984E-1, 1.27536670759978104416E0,
                  5.01905042251180477414E0,  6.16021097993053585195E0,
                  7.40974269950448939160E0,  2.97886665372100240670E0};
    scalar S[] = {// 1.00000000000000000000E0,
                  2.26052863220117276590E0, 9.39603524938001434673E0,
                  1.20489539808096656605E1, 1.70814450747565897222E1,
                  9.60896809063285878198E0, 3.36907645100081516050E0};
    if (a < 0.0)
        x = -a;
    else
        x = a;
    if (x < 1.0)
        return 1.0 - erf(a);
    z = -a * a;
    if (z < -M_MAXLOG)
    {
        if (a < 0)
            return (2.0);
        else
            return (0.0);
    }
    z = exp(z);
    if (x < 8.0)
    {
        p = polevl(x, P, 8);
        q = p1evl(x, Q, 8);
    }
    else
    {
        p = polevl(x, R, 5);
        q = p1evl(x, S, 6);
    }
    y = (z * p) / q;
    if (a < 0)
        y = 2.0 - y;
    if (y == 0.0)
    {
        if (a < 0)
            return 2.0;
        else
            return (0.0);
    }
    return y;
}
INLINE scalar erf(scalar x)
{
    scalar y, z;
    scalar T[5] = {9.60497373987051638749E0, 9.00260197203842689217E1,
                   2.23200534594684319226E3, 7.00332514112805075473E3,
                   5.55923013010394962768E4};
    scalar U[5] = {3.35617141647503099647E1, 5.21357949780152679795E2,
                   4.59432382970980127987E3, 2.26290000613890934246E4,
                   4.92673942608635921086E4};
    if (abs(x) > 1.0)
        return 1.0 - erfc(x);
    z = x * x;
    y = x * polevl(z, T, 4) / p1evl(z, U, 5);
    return y;
}
#endif
INLINE scalar max(scalar x, scalar y) { return x > y ? x : y; }
INLINE scalar min(scalar x, scalar y) { return x > y ? y : x; }
INLINE scalar saturate(scalar x) { return x < 0 ? 0 : x > 1 ? 1 : x; }
INLINE scalar rsqrt(scalar x) { return 1.0f / sqrtf(x); }

#ifdef CPLUSPLUS11
double erf(double x);
#endif

#ifdef _MSC_VER
// Microsoft C library lacks log1p
INLINE double log1p(double x)
{
    if (fabs(x) > 1e-4)
        return log(1.0 + x);
    return (-0.5 * x + 1.0) *
           x; // Taylor approx of log(1.0 + x) - more accurate for small values
}
#endif
INLINE scalar erfinv(scalar _a)
{
    double a = (double)_a;
    double p, q, t, fa;
    volatile union
    {
        double d;
        unsigned long long int l;
    } cvt;

    fa = fabs(a);
    if (fa >= 1.0)
    {
        cvt.l = 0xfff8000000000000ull;
        t = cvt.d; /* INDEFINITE */
        if (fa == 1.0)
        {
            t = a * exp(1000.0); /* Infinity */
        }
    }
    else if (fa >= 0.9375)
    {
        /* Based on: J.M. Blair, C.A. Edwards, J.H. Johnson: Rational Chebyshev
           Approximations for the Inverse of the Error Function. Mathematics of
           Computation, Vol. 30, No. 136 (Oct. 1976), pp. 827-830. Table 59
         */
        t = log1p(-fa);
        t = 1.0 / sqrt(-t);
        p = 2.7834010353747001060e-3;
        p = p * t + 8.6030097526280260580e-1;
        p = p * t + 2.1371214997265515515e+0;
        p = p * t + 3.1598519601132090206e+0;
        p = p * t + 3.5780402569085996758e+0;
        p = p * t + 1.5335297523989890804e+0;
        p = p * t + 3.4839207139657522572e-1;
        p = p * t + 5.3644861147153648366e-2;
        p = p * t + 4.3836709877126095665e-3;
        p = p * t + 1.3858518113496718808e-4;
        p = p * t + 1.1738352509991666680e-6;
        q = t + 2.2859981272422905412e+0;
        q = q * t + 4.3859045256449554654e+0;
        q = q * t + 4.6632960348736635331e+0;
        q = q * t + 3.9846608184671757296e+0;
        q = q * t + 1.6068377709719017609e+0;
        q = q * t + 3.5609087305900265560e-1;
        q = q * t + 5.3963550303200816744e-2;
        q = q * t + 4.3873424022706935023e-3;
        q = q * t + 1.3858762165532246059e-4;
        q = q * t + 1.1738313872397777529e-6;
        t = p / (q * t);
        if (a < 0.0)
            t = -t;
    }
    else if (fa >= 0.75)
    {
        /* Based on: J.M. Blair, C.A. Edwards, J.H. Johnson: Rational Chebyshev
           Approximations for the Inverse of the Error Function. Mathematics of
           Computation, Vol. 30, No. 136 (Oct. 1976), pp. 827-830. Table 39
        */
        t = a * a - .87890625;
        p = .21489185007307062000e+0;
        p = p * t - .64200071507209448655e+1;
        p = p * t + .29631331505876308123e+2;
        p = p * t - .47644367129787181803e+2;
        p = p * t + .34810057749357500873e+2;
        p = p * t - .12954198980646771502e+2;
        p = p * t + .25349389220714893917e+1;
        p = p * t - .24758242362823355486e+0;
        p = p * t + .94897362808681080020e-2;
        q = t - .12831383833953226499e+2;
        q = q * t + .41409991778428888716e+2;
        q = q * t - .53715373448862143349e+2;
        q = q * t + .33880176779595142685e+2;
        q = q * t - .11315360624238054876e+2;
        q = q * t + .20369295047216351160e+1;
        q = q * t - .18611650627372178511e+0;
        q = q * t + .67544512778850945940e-2;
        p = p / q;
        t = a * p;
    }
    else
    {
        /* Based on: J.M. Blair, C.A. Edwards, J.H. Johnson: Rational Chebyshev
           Approximations for the Inverse of the Error Function. Mathematics of
           Computation, Vol. 30, No. 136 (Oct. 1976), pp. 827-830. Table 18
        */
        t = a * a - .5625;
        p = -.23886240104308755900e+2;
        p = p * t + .45560204272689128170e+3;
        p = p * t - .22977467176607144887e+4;
        p = p * t + .46631433533434331287e+4;
        p = p * t - .43799652308386926161e+4;
        p = p * t + .19007153590528134753e+4;
        p = p * t - .30786872642313695280e+3;
        q = t - .83288327901936570000e+2;
        q = q * t + .92741319160935318800e+3;
        q = q * t - .35088976383877264098e+4;
        q = q * t + .59039348134843665626e+4;
        q = q * t - .48481635430048872102e+4;
        q = q * t + .18997769186453057810e+4;
        q = q * t - .28386514725366621129e+3;
        p = p / q;
        t = a * p;
    }
    return (scalar)t;
}
INLINE scalar erfcinv(scalar _a)
{
    double a = (double)_a;
    double t;
    volatile union
    {
        double d;
        unsigned long long int l;
    } cvt;

#if defined(_MSC_VER)
    if (_isnan(a))
#else
    if (isnan(a))
#endif
    {
        return a + a;
    }
    if (a <= 0.0)
    {
        cvt.l = 0xfff8000000000000ull;
        t = cvt.d; /* INDEFINITE */
        if (a == 0.0)
        {
            t = (1.0 - a) * exp(1000.0); /* Infinity */
        }
    }
    else if (a >= 0.0625)
    {
        t = erfinv(1.0 - a);
    }
    else if (a >= 1e-100)
    {
        /* Based on: J.M. Blair, C.A. Edwards, J.H. Johnson: Rational Chebyshev
           Approximations for the Inverse of the Error Function. Mathematics of
           Computation, Vol. 30, No. 136 (Oct. 1976), pp. 827-830. Table 59
        */
        double p, q;
        t = log(a);
        t = 1.0 / sqrt(-t);
        p = 2.7834010353747001060e-3;
        p = p * t + 8.6030097526280260580e-1;
        p = p * t + 2.1371214997265515515e+0;
        p = p * t + 3.1598519601132090206e+0;
        p = p * t + 3.5780402569085996758e+0;
        p = p * t + 1.5335297523989890804e+0;
        p = p * t + 3.4839207139657522572e-1;
        p = p * t + 5.3644861147153648366e-2;
        p = p * t + 4.3836709877126095665e-3;
        p = p * t + 1.3858518113496718808e-4;
        p = p * t + 1.1738352509991666680e-6;
        q = t + 2.2859981272422905412e+0;
        q = q * t + 4.3859045256449554654e+0;
        q = q * t + 4.6632960348736635331e+0;
        q = q * t + 3.9846608184671757296e+0;
        q = q * t + 1.6068377709719017609e+0;
        q = q * t + 3.5609087305900265560e-1;
        q = q * t + 5.3963550303200816744e-2;
        q = q * t + 4.3873424022706935023e-3;
        q = q * t + 1.3858762165532246059e-4;
        q = q * t + 1.1738313872397777529e-6;
        t = p / (q * t);
    }
    else
    {
        /* Based on: J.M. Blair, C.A. Edwards, J.H. Johnson: Rational Chebyshev
           Approximations for the Inverse of the Error Function. Mathematics of
           Computation, Vol. 30, No. 136 (Oct. 1976), pp. 827-830. Table 82
        */
        double p, q;
        t = log(a);
        t = 1.0 / sqrt(-t);
        p = 6.9952990607058154858e-1;
        p = p * t + 1.9507620287580568829e+0;
        p = p * t + 8.2810030904462690216e-1;
        p = p * t + 1.1279046353630280005e-1;
        p = p * t + 6.0537914739162189689e-3;
        p = p * t + 1.3714329569665128933e-4;
        p = p * t + 1.2964481560643197452e-6;
        p = p * t + 4.6156006321345332510e-9;
        p = p * t + 4.5344689563209398450e-12;
        q = t + 1.5771922386662040546e+0;
        q = q * t + 2.1238242087454993542e+0;
        q = q * t + 8.4001814918178042919e-1;
        q = q * t + 1.1311889334355782065e-1;
        q = q * t + 6.0574830550097140404e-3;
        q = q * t + 1.3715891988350205065e-4;
        q = q * t + 1.2964671850944981713e-6;
        q = q * t + 4.6156017600933592558e-9;
        q = q * t + 4.5344687377088206783e-12;
        t = p / (q * t);
    }
    return (scalar)t;
}
#ifndef CPLUSPLUS11
INLINE scalar lgamma(scalar x)
{ // Natural logarithm of the gamma function
    scalar p, q, w, z;
    scalar A[] = {8.11614167470508450300E-4, -5.95061904284301438324E-4,
                  7.93650340457716943945E-4, -2.77777777730099687205E-3,
                  8.33333333333331927722E-2};
    scalar B[] = {-1.37825152569120859100E3, -3.88016315134637840924E4,
                  -3.31612992738871184744E5, -1.16237097492762307383E6,
                  -1.72173700820839662146E6, -8.53555664245765465627E5};
    scalar C[] = {/* 1.00000000000000000000E0, */
                  -3.51815701436523470549E2, -1.70642106651881159223E4,
                  -2.20528590553854454839E5, -1.13933444367982507207E6,
                  -2.53252307177582951285E6, -2.01889141433532773231E6};

    if (x < -34.0)
    {
        q = -x;
        w = lgamma(q);
        p = floor(q);
        if (p == q)
            return NAN;
        z = q - p;
        if (z > 0.5)
        {
            p += 1.0;
            z = p - q;
        }
        z = q * sin(M_PI * z);
        if (z == 0.0)
            return NAN;
        z = M_LOGPI - log(z) - w;
        return z;
    }

    if (x < 13.0)
    {
        z = 1.0;
        while (x >= 3.0)
        {
            x -= 1.0;
            z *= x;
        }
        while (x < 2.0)
        {
            if (x == 0.0)
                return NAN;
            z /= x;
            x += 1.0;
        }
        if (z < 0.0)
            z = -z;
        if (x == 2.0)
            return log(z);
        x -= 2.0;
        p = x * polevl(x, B, 5) / p1evl(x, C, 6);
        return (log(z) + p);
    }

    if (x > 2.556348e305)
        return NAN;
    q = (x - 0.5) * log(x) - x + 0.91893853320467274178;
    if (x > 1.0e8)
        return (q);

    p = 1.0 / (x * x);
    if (x >= 1000.0)
        q += ((7.9365079365079365079365e-4 * p - 2.7777777777777777777778e-3) *
                  p +
              0.0833333333333333333333) /
             x;
    else
        q += polevl(p, A, 4) / x;
    return q;
}

INLINE scalar tgamma_helper(scalar a)
{
    scalar t;
    t = -1.05767296987211380E-003;
    t = t * a + 7.09279059435508670E-003;
    t = t * a - 9.65347121958557050E-003;
    t = t * a - 4.21736613253687960E-002;
    t = t * a + 1.66542401247154280E-001;
    t = t * a - 4.20043267827838460E-002;
    t = t * a - 6.55878234051332940E-001;
    t = t * a + 5.77215696929794240E-001;
    t = t * a + 1;
    return t;
}

INLINE scalar tgamma(scalar a)
{
    float s, xx, x = a;
    if (x >= 0.0f)
    {
        if (x > 36.0f)
            x = 36.0f; /* clamp */
        s = 1.0f;
        xx = x;
        if (x > 34.03f)
        { /* prevent premature overflow */
            xx -= 1.0f;
        }
        while (xx > 1.5f)
        {
            xx = xx - 1.0f;
            s = s * xx;
        }
        if (x >= 0.5f)
        {
            xx = xx - 1.0f;
        }
        xx = tgamma_helper(xx);
        if (x < 0.5f)
        {
            xx = xx * x;
        }
        s = s / xx;
        if (x > 34.03f)
        {
            /* Cannot use s = s * x - s due to intermediate overflow! */
            xx = x - 1.0f;
            s = s * xx;
        }
        return s;
    }
    else
    {
        if (x == floor(x))
        { /* x is negative integer */
            x = NAN;
        }
        if (x < -41.1f)
            x = -41.1f; /* clamp */
        xx = x;
        if (x < -34.03f)
        { /* prevent overflow in intermediate result */
            xx += 6.0f;
        }
        s = xx;
        while (xx < -0.5f)
        {
            xx = xx + 1.0f;
            s = s * xx;
        }
        xx = tgamma_helper(xx);
        s = s * xx;
        s = 1.0f / s;
        if (x < -34.03f)
        {
            xx = x;
            xx *= (x + 1.0f);
            xx *= (x + 2.0f);
            xx *= (x + 3.0f);
            xx *= (x + 4.0f);
            xx *= (x + 5.0f);
            xx = 1.0f / xx;
            s = s * xx;
            if ((a < -42.0f) && !(((int)a) & 1))
            {
                s = -0.0f;
            }
        }
        return s;
    }
}
#endif

#if defined(_MSC_VER)

INLINE int _isfinite_(scalar x) { return _finite(x); }
INLINE int _isinf_(scalar x) { return !(_finite(x) || _isnan(x)); }
INLINE int _isnan_(scalar x) { return _isnan(x); }

// GCC seems to define isfinite, isnan and isinf through macros, which causes
// some problems when applied to cscalar values/vectors, therefore we renamed
// these functions to _isfinite_, _isnan_, _isinf_
#elif defined(__GNUG__)

INLINE int _isfinite_(scalar x) { return finite(x); }
INLINE int _isinf_(scalar x) { return !(finite(x) || isnan(x)); }
INLINE int _isnan_(scalar x) { return isnan(x); }

#endif

// All different overloads need to be defined, otherwise
// the compiler complains...
INLINE int _isfinite_(int8_t x) { return 0; }
INLINE int _isinf_(int8_t x) { return 0; }
INLINE int _isnan_(int8_t x) { return 0; }
INLINE int _isfinite_(uint8_t x) { return 0; }
INLINE int _isinf_(uint8_t x) { return 0; }
INLINE int _isnan_(uint8_t x) { return 0; }
INLINE int _isfinite_(int16_t x) { return 0; }
INLINE int _isinf_(int16_t x) { return 0; }
INLINE int _isnan_(int16_t x) { return 0; }
INLINE int _isfinite_(uint16_t x) { return 0; }
INLINE int _isinf_(uint16_t x) { return 0; }
INLINE int _isnan_(uint16_t x) { return 0; }
INLINE int _isfinite_(int32_t x) { return 0; }
INLINE int _isinf_(int32_t x) { return 0; }
INLINE int _isnan_(int32_t x) { return 0; }
INLINE int _isfinite_(uint32_t x) { return 0; }
INLINE int _isinf_(uint32_t x) { return 0; }
INLINE int _isnan_(uint32_t x) { return 0; }
INLINE int _isfinite_(int64_t x) { return 0; }
INLINE int _isinf_(int64_t x) { return 0; }
INLINE int _isnan_(int64_t x) { return 0; }
INLINE int _isfinite_(uint64_t x) { return 0; }
INLINE int _isinf_(uint64_t x) { return 0; }
INLINE int _isnan_(uint64_t x) { return 0; }


#define __cosf cosf
#define __sinf sinf

#undef assert
INLINE void assert(bool x)
{
    if (!(x))
    {
        throw "Kernel error";
    }
}

#else // if defined(TARGET_CUDA)

// All different overloads need to be defined, otherwise
// the compiler complains...
INLINE int __device__ _isfinite_(scalar x) { return isfinite(x); }
INLINE int __device__ _isnan_(scalar x) { return isnan(x); }
INLINE int __device__ _isinf_(scalar x) { return isinf(x); }
INLINE int __device__ _isfinite_(int8_t x) { return 0; }
INLINE int __device__ _isinf_(int8_t x) { return 0; }
INLINE int __device__ _isnan_(int8_t x) { return 0; }
INLINE int __device__ _isfinite_(uint8_t x) { return 0; }
INLINE int __device__ _isinf_(uint8_t x) { return 0; }
INLINE int __device__ _isnan_(uint8_t x) { return 0; }
INLINE int __device__ _isfinite_(int16_t x) { return 0; }
INLINE int __device__ _isinf_(int16_t x) { return 0; }
INLINE int __device__ _isnan_(int16_t x) { return 0; }
INLINE int __device__ _isfinite_(uint16_t x) { return 0; }
INLINE int __device__ _isinf_(uint16_t x) { return 0; }
INLINE int __device__ _isnan_(uint16_t x) { return 0; }
INLINE int __device__ _isfinite_(int32_t x) { return 0; }
INLINE int __device__ _isinf_(int32_t x) { return 0; }
INLINE int __device__ _isnan_(int32_t x) { return 0; }
INLINE int __device__ _isfinite_(uint32_t x) { return 0; }
INLINE int __device__ _isinf_(uint32_t x) { return 0; }
INLINE int __device__ _isnan_(uint32_t x) { return 0; }
INLINE int __device__ _isfinite_(int64_t x) { return 0; }
INLINE int __device__ _isinf_(int64_t x) { return 0; }
INLINE int __device__ _isnan_(int64_t x) { return 0; }
INLINE int __device__ _isfinite_(uint64_t x) { return 0; }
INLINE int __device__ _isinf_(uint64_t x) { return 0; }
INLINE int __device__ _isnan_(uint64_t x) { return 0; }

// Implement assert through the trap instruction
// See
// https://devtalk.nvidia.com/default/topic/491119/cuda-programming-and-performance/using-assert-in-cuda-code/
#undef assert

// Uncommented, because CUDA cannot recover from a trap:(
//#define assert(x)	if (!(x)) { __threadfence(); asm("trap;"); }
#define assert(x)

//+++++++ ENDIF TARGET_CUDA +++++++++
#endif

// Custom implementation of a number of functions that seem to be missing
INLINE __device__ uint8_t abs(uint8_t a) { return a; }
INLINE __device__ uint16_t abs(uint16_t a) { return a; }
INLINE __device__ uint32_t abs(uint32_t a) { return a; }
INLINE __device__ uint64_t abs(uint64_t a) { return a; }

INLINE __device__ uint32_t mod(uint32_t a, uint32_t b) { return a % b; }
INLINE __device__ uint64_t mod(uint64_t a, uint64_t b) { return a % b; }

INLINE __device__ int pow(int base, int a) // Exponentiation by squaring
{
    int res = 1;
    while (a)
    {
        if (a & 1)
            res *= base;
        a >>= 1;
        base *= base;
    }
    return res;
}
INLINE __device__ unsigned int pow(unsigned int base,
                                   unsigned int a) // Exponentiation by squaring
{
    unsigned int res = 1;
    while (a)
    {
        if (a & 1)
            res *= base;
        a >>= 1;
        base *= base;
    }
    return res;
}
INLINE __device__ scalar pow(int a, scalar b) { return powf(a, b); }

#ifdef _MSC_VER
INLINE __device__ int floor(int x)
{
    return x;
} // MSVC does not know about this overload
INLINE __device__ scalar log(int x)
{
    return log((scalar)x);
} // MSVC does not know about this overload
INLINE __device__ scalar exp(int x)
{
    return exp((scalar)x);
} // MSVC does not know about this overload
#endif

/* Built-in types */
/* At some point in the future, we may use C++03 typedef templates here... */
template <int N> struct intN
{
    int el[N];
    __device__ operator quasar::VectorBase<int>()
    {
        return quasar::VectorBase<int>(el, N);
    }
};
template <int N> struct scalarN
{
    scalar el[N];
    __device__ operator quasar::VectorBase<scalar>()
    {
        return quasar::VectorBase<scalar>(el, N);
    }
};

// Need implicit conversion from int4 to intN<4>. This is required for several
// higher dimensional functions. We obtain this by specializing the intN<4>
// class.
template <> struct intN<4>
{
    int el[4];
    __device__ intN() {}
    __device__ operator quasar::VectorBase<int>()
    {
        return quasar::VectorBase<int>(el, 4);
    }
    __device__ intN(int4 a)
    {
        el[0] = a.x;
        el[1] = a.y;
        el[2] = a.z;
        el[3] = a.w;
    }
};

template <typename T, int N> struct vecTN
{
    T el[N];
    __device__ operator quasar::VectorBase<T>() const
    {
        return quasar::VectorBase<T>(el, N);
    }
    __device__ operator intN<N>() const
    {
        intN<N> y;
        for (int i = 0; i < N; i++)
            y.el[i] = __cast_int(el[i]);
        return y;
    }
};

template <typename T> struct vecTN<T, 1>
{
    T el[1];
    __device__ operator quasar::VectorBase<T>() const
    {
        return quasar::VectorBase<T>(el, 1);
    }
    __device__ operator int1() const { return make_int1(__cast_int(el[0])); }
};

template <typename T> struct vecTN<T, 2>
{
    T el[2];
    __device__ operator quasar::VectorBase<T>() const
    {
        return quasar::VectorBase<T>(el, 2);
    }
    __device__ operator int2() const
    {
        return make_int2(__cast_int(el[0]), __cast_int(el[1]));
    }
};

template <typename T> struct vecTN<T, 3>
{
    T el[3];
    __device__ operator quasar::VectorBase<T>() const
    {
        return quasar::VectorBase<T>(el, 3);
    }
    __device__ operator int3() const
    {
        return make_int3(__cast_int(el[0]), __cast_int(el[1]),
                         __cast_int(el[2]));
    }
};

template <typename T> struct vecTN<T, 4>
{
    T el[4];
    __device__ operator quasar::VectorBase<T>() const
    {
        return quasar::VectorBase<T>(el, 4);
    }
    __device__ operator int4() const
    {
        return make_int4(__cast_int(el[0]), __cast_int(el[1]),
                         __cast_int(el[2]), __cast_int(el[3]));
    }
};

template <int M, int N> struct scalarMxN
{
    scalar el[M][N];
};

// ARRAY_LENGTH macro. Warning: not to be used for function parameters (since
// they are pointers in C/C++)
#define ARRAY_LENGTH(x, y) (sizeof(x) / sizeof(y))

//=============================================================================
// Some custom functions
//=============================================================================
INLINE scalar __device__ frac(scalar x) { return x - floor(x); }
INLINE scalar __device__ frac(int x) { return 0; }
INLINE scalar __device__ mod(scalar x, scalar y) { return fmod(x, y); }
INLINE scalar __device__ sign(scalar x)
{
    return (x == 0) ? 0 : copysign(1.0f, x);
}
INLINE int __device__ sign(int x) { return (x == 0) ? 0 : (x < 0) ? -1 : 1; }
INLINE int __device__ sign(uint32_t x) { return (x == 0) ? 0 : 1; }
INLINE scalar __device__ sdiv(int x, int y) { return (scalar)x / (scalar)y; }
INLINE int __device__ idiv(int x, int y) { return x / y; }
INLINE int __device__ mod(int x, int y) { return x % y; }
INLINE scalar __device__ sum(scalar x) { return x; }
INLINE scalar __device__ sum(scalar1 a) { return a.x; }
INLINE scalar __device__ sum(scalar2 a) { return a.x + a.y; }
INLINE scalar __device__ sum(scalar3 a) { return a.x + a.y + a.z; }
INLINE scalar __device__ sum(scalar4 a) { return a.x + a.y + a.z + a.w; }
INLINE int __device__ sum(int x) { return x; }
INLINE int __device__ sum(int1 a) { return a.x; }
INLINE int __device__ sum(int2 a) { return a.x + a.y; }
INLINE int __device__ sum(int3 a) { return a.x + a.y + a.z; }
INLINE int __device__ sum(int4 a) { return a.x + a.y + a.z + a.w; }
INLINE scalar __device__ prod(scalar x) { return x; }
INLINE scalar __device__ prod(scalar1 a) { return a.x; }
INLINE scalar __device__ prod(scalar2 a) { return a.x * a.y; }
INLINE scalar __device__ prod(scalar3 a) { return a.x * a.y * a.z; }
INLINE scalar __device__ prod(scalar4 a) { return a.x * a.y * a.z * a.w; }
INLINE int __device__ prod(int x) { return x; }
INLINE int __device__ prod(int1 a) { return a.x; }
INLINE int __device__ prod(int2 a) { return a.x * a.y; }
INLINE int __device__ prod(int3 a) { return a.x * a.y * a.z; }
INLINE int __device__ prod(int4 a) { return a.x * a.y * a.z * a.w; }
INLINE scalar __device__ mean(scalar x) { return x; }
INLINE scalar __device__ mean(scalar1 a) { return a.x; }
INLINE scalar __device__ mean(scalar2 a) { return scalar(0.5) * (a.x + a.y); }
INLINE scalar __device__ mean(scalar3 a)
{
    return scalar(1.0 / 3) * (a.x + a.y + a.z);
}
INLINE scalar __device__ mean(scalar4 a)
{
    return scalar(1.0 / 4) * (a.x + a.y + a.z + a.w);
}
INLINE scalar __device__ mean(int x) { return x; }
INLINE scalar __device__ mean(int1 a) { return a.x; }
INLINE scalar __device__ mean(int2 a) { return scalar(0.5) * (a.x + a.y); }
INLINE scalar __device__ mean(int3 a)
{
    return scalar(1.0 / 3) * (a.x + a.y + a.z);
}
INLINE scalar __device__ mean(int4 a)
{
    return scalar(1.0 / 4) * (a.x + a.y + a.z + a.w);
}
template <typename T, typename R> INLINE T __device__ _and_(T a, R b)
{
    return (T)(a & b);
}
template <typename T, typename R> INLINE T __device__ _or_(T a, R b)
{
    return (T)(a | b);
}
template <typename T, typename R> INLINE T __device__ _xor_(T a, R b)
{
    return (T)(a ^ b);
}
template <typename T, typename R> INLINE T __device__ _shl_(T a, R b)
{
    return (T)(a << b);
}
template <typename T, typename R> INLINE T __device__ _shr_(T a, R b)
{
    return (T)(a >> b);
}
template <typename T> INLINE T __device__ _not_(T a) { return ~a; }
template <> INLINE scalar __device__ _and_(scalar a, scalar b)
{
    return (scalar)((int)a & (int)b);
}
template <> INLINE scalar __device__ _or_(scalar a, scalar b)
{
    return (scalar)((int)a | (int)b);
}
template <> INLINE scalar __device__ _xor_(scalar a, scalar b)
{
    return (scalar)((int)a ^ (int)b);
}
template <> INLINE scalar __device__ _shl_(scalar a, scalar b)
{
    return (scalar)((int)a << (int)b);
}
template <> INLINE scalar __device__ _shr_(scalar a, scalar b)
{
    return (scalar)((int)a >> (int)b);
}
template <> INLINE scalar __device__ _not_(scalar a)
{
    return (scalar) ~(int)a;
}
INLINE int __device__ _any_(int a) { return a != 0; }
INLINE int __device__ _any_(int2 a) { return a.x || a.y; }
INLINE int __device__ _any_(int3 a) { return a.x || a.y || a.z; }
INLINE int __device__ _any_(int4 a) { return a.x || a.y || a.z || a.w; }
INLINE int __device__ _all_(int a) { return a != 0; }
INLINE int __device__ _all_(int2 a) { return a.x && a.y; }
INLINE int __device__ _all_(int3 a) { return a.x && a.y && a.z; }
INLINE int __device__ _all_(int4 a) { return a.x && a.y && a.z && a.w; }
INLINE int __device__ ind2pos(int sz, int a) { return a; }
INLINE int2 __device__ ind2pos(int2 sz, int a)
{
    return make_int2(a / sz.y, a % sz.y);
}
INLINE int3 __device__ ind2pos(int3 sz, int a)
{
    int3 u;
    u.z = a % sz.z;
    a /= sz.z;
    u.y = a % sz.y;
    a /= sz.y;
    u.x = a;
    return u;
}
INLINE int4 __device__ ind2pos(int4 sz, int a)
{
    int4 u;
    u.w = a % sz.w;
    a /= sz.w;
    u.z = a % sz.z;
    a /= sz.z;
    u.y = a % sz.y;
    a /= sz.y;
    u.x = a;
    return u;
}
template <int N> INLINE intN<N> __device__ ind2pos(intN<N> sz, int a)
{
    intN<N> u;
    for (int i = N - 1; i >= 1; i--)
    {
        u.el[i] = a % sz.el[i];
        a /= sz.el[i];
    }
    u.el[0] = a;
    return u;
}
INLINE int __device__ pos2ind(int sz, int pos) { return pos; }
INLINE int __device__ pos2ind(int2 sz, int2 pos)
{
    return pos.x * sz.y + pos.y;
}
INLINE int __device__ pos2ind(int3 sz, int3 pos)
{
    return (pos.x * sz.y + pos.y) * sz.z + pos.z;
}
INLINE int __device__ pos2ind(int4 sz, int4 pos)
{
	return ((pos.x * sz.y + pos.y) * sz.z + pos.z) * sz.w + pos.w;
}
template <int N> INLINE int __device__ pos2ind(intN<N> sz, intN<N> pos)
{
    int ind = pos.el[0];
    for (int i = 1; i < N; i++)
    {
        ind *= sz.el[i];
        ind += pos.el[i];
    }
    return ind;
}

template <int N> INLINE scalar __device__ sum(const scalarN<N> &a)
{
    scalar s = 0.0f;
    for (int i = 0; i < N; i++)
        s += a.el[i];
    return s;
}

template <int N> INLINE int __device__ sum(intN<N> a)
{
    int s = 0;
    for (int i = 0; i < N; i++)
        s += a.el[i];
    return s;
}

template <int N> INLINE scalar __device__ prod(const scalarN<N> &a)
{
    scalar s = 1.0f;
    for (int i = 0; i < N; i++)
        s *= a.el[i];
    return s;
}

template <int N> INLINE int __device__ prod(intN<N> a)
{
    int s = 1;
    for (int i = 0; i < N; i++)
        s *= a.el[i];
    return s;
}

template <int N> INLINE int __device__ _any_(intN<N> a)
{
    int s = 0;
    for (int i = 0; i < N; i++)
        s |= a.el[i];
    return s;
}

template <int N> INLINE int __device__ _all_(intN<N> a)
{
    int s = 1;
    for (int i = 0; i < N; i++)
        s &= a.el[i];
    return s;
}


//=============================================================================
// SUPPORT FOR HALF PRECISION FLOATING POINT NUMBERS
//=============================================================================

#if !defined(__CUDA_ARCH__)
// CPU replacement for the __float2half_rn function.
INLINE __host__ __device__ uint16_t float2half_rn(float f)
{
    uint32_t inu = *(uint32_t *) &f;
    uint32_t t1 = inu & 0x7fffffff;             // Non-sign bits
    uint32_t t2 = inu & 0x80000000;             // Sign bit
    uint32_t t3 = inu & 0x7f800000;             // Exponent
    t1 >>= 13;                                  // Align mantissa on MSB
    t2 >>= 16;                                  // Shift sign bit into position
    t1 -= 0x1c000;                              // Adjust bias
    t1 = (t3 < 0x38800000) ? 0 : t1;            // Flush-to-zero
    t1 = (t3 > 0x8e000000) ? 0x7bff : t1;       // Clamp-to-max
    t1 = (t3 == 0 ? 0 : t1);                    // Denormals-as-zero
    t1 |= t2;                                   // Re-insert sign bit
    return (uint16_t) t1;
}

// CPU replacement for the __half2float function.
INLINE __host__ __device__ float half2float(uint16_t value)
{
	uint32_t inu = (uint32_t)value;
    uint32_t t1 = inu & 0x7fffu;                // Non-sign bits
    uint32_t t2 = inu & 0x8000u;                // Sign but
    uint32_t t3 = inu & 0x7c00u;                // Exponent
    t1 = t1 << 13;                              // Align mantissa on MSB
    t2 = t2 << 16;                              // Shift sign bit into position
    t1 += 0x38000000u;                          // Adjust bias
    t1 = (t3 == 0 ? 0 : t1);                    // Denormals-as-zero
    t1 |= t2;                                   // Reinsert sign bit
    float f = 0.0f;
    *(uint32_t*)&f = t1;
    return f;
}

#else
#define float2half_rn __float2half_rn
#define half2float __half2float
#endif

struct half_t
{
	uint16_t value;
	INLINE __host__ __device__ half_t() : value(0) {}
	INLINE __host__ __device__ half_t(float f) : value(float2half_rn(f)) {}
	INLINE __host__ __device__ operator float() const { return half2float(value); }
};

// Vector types
struct half_t1
{
    half_t x;

	INLINE __host__ __device__ half_t1() : x(0) {}
	INLINE __host__ __device__ half_t1(scalar1 a) : x(a.x) {}
	INLINE __host__ __device__ half_t1(half_t a) : x(a) {}
	INLINE __host__ __device__ operator scalar1() const { return make_scalar1(x); }
};
struct half_t2
{
    half_t x, y;

	INLINE __host__ __device__ half_t2() : x(0), y(0) {}
	INLINE __host__ __device__ half_t2(scalar2 a) : x(a.x), y(a.y) {}	
	INLINE __host__ __device__ half_t2(half_t a, half_t b) : x(a), y(b) {}
	INLINE __host__ __device__ operator scalar2() const { return make_scalar2(x, y); }
};
struct half_t3
{
    half_t x, y, z;

	INLINE __host__ __device__ half_t3() : x(0), y(0), z(0) {}
	INLINE __host__ __device__ half_t3(scalar3 a) : x(a.x), y(a.y), z(a.z) {}	
	INLINE __host__ __device__ half_t3(half_t a, half_t b, half_t c) : x(a), y(b), z(c) {}
	INLINE __host__ __device__ operator scalar3() const { return make_scalar3(x, y, z); }
};
struct half_t4
{
    half_t x, y, z, w;

	INLINE __host__ __device__ half_t4() : x(0), y(0), z(0), w(0) {}
	INLINE __host__ __device__ half_t4(scalar4 a) : x(a.x), y(a.y), z(a.z), w(a.w) {}	
	INLINE __host__ __device__ half_t4(half_t a, half_t b, half_t c, half_t d) : x(a), y(b), z(c), w(d) {}
	INLINE __host__ __device__ operator scalar4() const { return make_scalar4(x, y, z, w); }
};
template <int N> struct half_tN
{
    half_t el[N];

	INLINE __host__ __device__ half_tN()  
	{
		for (int i = 0; i < N; i ++) el[i] = 0;
	}
	INLINE __host__ __device__ half_tN(scalarN<N> a)
	{
		for (int i = 0; i < N; i ++) el[i] = a.el[i];
	}	
	INLINE __host__ __device__ operator scalarN<N>() const { 
		scalarN<N> y;
		for (int i = 0; i < N; i ++)
			y.el[i] = (scalar) el[i];
		return y;
	}
};

INLINE __device__ half_t1 make_half_t1(half_t x)
{
    return half_t1 (x);
}
INLINE __device__ half_t2 make_half_t2(half_t x, half_t y)
{
    return half_t2 (x, y);
}
INLINE __device__ half_t3 make_half_t3(half_t x, half_t y, half_t z)
{
    return half_t3 (x, y, z);
}
INLINE __device__ half_t4 make_half_t4(half_t x, half_t y, half_t z, half_t w)
{
	return half_t4 (x, y, z, w);
}


namespace quasar 
{
#if !defined(HALF_SCALAR)
    typedef VectorBase<scalar> Vector;
    typedef MatrixBase<scalar> Matrix;
    typedef CubeBase<scalar> Cube;
#else
    typedef VectorBase<half_t> Vector;
    typedef MatrixBase<half_t> Matrix;
    typedef CubeBase<half_t> Cube;
#endif

} // namespace quasar


//=============================================================================
// SUPPORT FOR COMPLEX NUMBERS
//=============================================================================
template <typename T>
struct cscalar_t
{
    T x;
    T y;

    INLINE __device__ cscalar_t() : x(0), y(0) {}
    INLINE __device__ cscalar_t(T x) : x(x), y(0) {}
    INLINE __device__ cscalar_t(T x, T y) : x(x), y(y) {}    
    //INLINE __device__ cscalar(const cscalar &b) : x(b.x), y(b.y) {}
    INLINE __device__ cscalar_t operator*(cscalar_t b) const
    {
        return cscalar_t(x * b.x - y * b.y, x * b.y + y * b.x);
    }
    INLINE __device__ cscalar_t operator+(cscalar_t b) const
    {
        return cscalar_t(x + b.x, y + b.y);
    }
    INLINE __device__ cscalar_t operator-(cscalar_t b) const
    {
        return cscalar_t(x - b.x, y - b.y);
    }
    INLINE __device__ cscalar_t operator/(cscalar_t b) const
    {
        if (abs(b.x) < abs(b.y))
        { // implementation which is overflow-safe
            T t = b.x / b.y;
            T d = b.y * (1.0f + t * t);
            return (*this) * cscalar_t(t / d, -1.0f / d);
        }
        else
        {
            T t = b.y / b.x;
            T d = b.x * (1.0f + t * t);
            return (*this) * cscalar_t(1.0f / d, -t / d);
        }
    }
    INLINE __device__ cscalar_t operator*=(cscalar_t b)
    {
        return *this = (*this * b);
    }
    INLINE __device__ cscalar_t operator+=(cscalar_t b)
    {
        return *this = (*this + b);
    }
    INLINE __device__ cscalar_t operator-=(cscalar_t b)
    {
        return *this = (*this - b);
    }
    INLINE __device__ cscalar_t operator/=(cscalar_t b)
    {
        return *this = (*this /= b);
    }
    INLINE __device__ int operator==(cscalar_t b) { return x == b.x && y == b.y; }
    INLINE __device__ int operator!=(cscalar_t b) { return x != b.x || y != b.y; }
    INLINE __device__ T square_mag() const { return x * x + y * y; }
    INLINE cscalar_t __device__ operator=(cscalar_t b)
    {
        x = b.x;
        y = b.y;
        return *this;
    }
	template <typename R>
	INLINE __device__ operator cscalar_t<R>() const {
		return cscalar_t<R>((R)x, (R)y);
	}
};

typedef cscalar_t<scalar> cscalar;

INLINE __device__ cscalar make_cscalar(scalar x, scalar y)
{
    return cscalar(x, y);
}
template <typename T>
INLINE __device__ T abs(cscalar_t<T> a) { return sqrt(a.square_mag()); }
INLINE __device__ scalar angle(scalar a) { return (scalar)0; }
template <typename T>
INLINE __device__ T angle(cscalar_t<T> a) { return atan2(a.y, a.x); }
INLINE __device__ scalar conj(scalar a) { return a; }
template <typename T>
INLINE __device__ cscalar_t<T> conj(cscalar_t<T> a) { return cscalar_t<T>(a.x, -a.y); }

// Operators
template <typename T>
INLINE __device__ cscalar_t<T> operator/(cscalar_t<T> a, T b)
{
    return cscalar_t<T>(a.x / b, a.y / b);
}
template <typename T>
INLINE __device__ cscalar_t<T> operator/(T a, cscalar_t<T> b)
{
    if (abs(b.x) < abs(b.y))
    { // implementation which is overflow-safe
        T t = b.x / b.y;
        T d = b.y * (1.0f + t * t);
        return cscalar_t<T>(a * t / d, -a / d);
    }
    else
    {
        T t = b.y / b.x;
        T d = b.x * (1.0f + t * t);
        return cscalar_t<T>(a / d, -a * t / d);
    }
}
template <typename T, typename R>
INLINE __device__ cscalar_t<T> operator*(cscalar_t<T> a, R b)
{
    return cscalar_t<T>(a.x * b, a.y * b);
}
template <typename T, typename R>
INLINE __device__ cscalar_t<T> operator*(R a, cscalar_t<T> b)
{
    return cscalar_t<T>(a * b.x, a * b.y);
}
template <typename T, typename R>
INLINE __device__ cscalar_t<T> operator+(cscalar_t<T> a, R b)
{
    return cscalar_t<T>(a.x + b, a.y);
}
template <typename T, typename R>
INLINE __device__ cscalar_t<T> operator+(R a, cscalar_t<T> b)
{
    return cscalar_t<T>(a + b.x, b.y);
}
template <typename T, typename R>
INLINE __device__ cscalar_t<T> operator-(cscalar_t<T> a, R b)
{
    return cscalar_t<T>(a.x - b, a.y);
}
template <typename T, typename R>
INLINE __device__ cscalar_t<T> operator-(R a, cscalar_t<T> b)
{
    return cscalar_t<T>(a - b.x, -b.y);
}
template <typename T>
INLINE __device__ cscalar_t<T> operator-(cscalar_t<T> a) { return cscalar_t<T>(-a.x, -a.y); }

template <typename T>
INLINE __device__ cscalar_t<T> pow(cscalar_t<T> a, cscalar_t<T> b)
{
    T rm = a.x * a.x + a.y * a.y;
    if (rm == 0)
        return cscalar_t<T>(0, 0);
    T r = log(rm);
    T t = atan2(a.y, a.x);
    T ra = exp(0.5f * b.x * r - b.y * t);
    T rb = 0.5 * b.y * r + b.x * t;
    return cscalar_t<T>(ra * __cosf(rb), ra * __sinf(rb));
}

template <typename T, typename R>
INLINE __device__ cscalar_t<T> pow(R a, cscalar_t<T> b)
{
    T r = log(a);
    T t = sign(a) * M_PI;
    T ra = exp(b.x * r - b.y * t);
    T rb = b.y * r + b.x * t;
    return cscalar_t<T>(ra * __cosf(rb), ra * __sinf(rb));
}

template <typename T, typename R>
INLINE __device__ cscalar_t<T> pow(cscalar_t<T> a, R b)
{
    T r = log(a.x * a.x + a.y * a.y);
    T t = atan2(a.y, a.x);
    T ra = exp(0.5f * b * r);
    T rb = b * t;
    return cscalar_t<T>(ra * __cosf(rb), ra * __sinf(rb));
}

template <typename T>
INLINE __device__ cscalar_t<T> log(cscalar_t<T> a)
{
    return cscalar_t<T>(0.5 * log(a.x * a.x + a.y * a.y), atan2(a.y, a.x));
}

template <typename T>
INLINE __device__ cscalar_t<T> log10(cscalar_t<T> a)
{
    T ln10 = log(10.0f);
    return cscalar_t<T>(0.5 * log(a.x * a.x + a.y * a.y) / ln10,
                   atan2(a.y, a.x) / ln10);
}

template <typename T>
INLINE __device__ cscalar_t<T> log2(cscalar_t<T> a)
{
    T ln2 = log(2.0f);
    return cscalar_t<T>(0.5 * log(a.x * a.x + a.y * a.y) / ln2,
                   atan2(a.y, a.x) / ln2);
}

template <typename T>
INLINE __device__ cscalar_t<T> sin(cscalar_t<T> a)
{
    return cscalar_t<T>(sin(a.x) * cosh(a.y), cos(a.x) * sinh(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> cos(cscalar_t<T> a)
{
    return cscalar_t<T>(cos(a.x) * cosh(a.y), -sin(a.x) * sinh(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> tan(cscalar_t<T> a)
{
    T cosr = cos(a.x);
    T sinhi = sinh(a.y);
    T denom = cosr * cosr + sinhi * sinhi;
    return cscalar_t<T>(sin(a.x) * cosr / denom, sinhi * cosh(a.y) / denom);
}

template <typename T>
INLINE __device__ cscalar_t<T> exp(cscalar_t<T> a)
{
    T expr = exp(a.x);
    return cscalar_t<T>(expr * cos(a.y), expr * sin(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> exp2(cscalar_t<T> a)
{
    T expr = exp2(a.x);
    T theta = M_LOG2 * a.y;
    return cscalar_t<T>(expr * cos(theta), expr * sin(theta));
}

template <typename T>
INLINE __device__ T sign(cscalar_t<T> a)
{
    return (a.x == 0 && a.y == 0) ? 0 : sign(a.x);
}

template <typename T>
INLINE __device__ cscalar_t<T> sqrt(cscalar_t<T> a)
{
    T r = sqrt(a.x * a.x + a.y * a.y);
    return cscalar_t<T>(sqrt(0.5 * (r + a.x)),
                   sqrt(0.5 * (r - a.x)) * (a.y >= 0 ? 1.0 : -1.0));
}

template <typename T>
INLINE __device__ cscalar_t<T> asin(cscalar_t<T> z)
{
    return cscalar_t<T>(0, -1) *
           log(cscalar_t<T>(0, 1) * z +
               sqrt(cscalar_t<T>(1) - z * z)); // -i*ln(iz+(1-z^2)^(1/2))
}

template <typename T>
INLINE __device__ cscalar_t<T> acos(cscalar_t<T> z)
{
    return cscalar_t<T>(0.5 * M_PI) - asin(z);
}

template <typename T>
INLINE __device__ cscalar_t<T> atan(cscalar_t<T> z)
{
    const cscalar_t<T> iz = cscalar_t<T>(0, 1) * z;
    return (cscalar_t<T>(0, 1) * log(cscalar_t<T>(1) - iz) - log(cscalar_t<T>(1) + iz)) * 0.5;
}

template <typename T>
INLINE __device__ T real(cscalar_t<T> a) { return a.x; }

template <typename T>
INLINE __device__ T imag(cscalar_t<T> a) { return a.y; }

template <typename T>
INLINE __device__ cscalar_t<T> ceil(cscalar_t<T> a)
{
    return cscalar_t<T>(ceil(a.x), ceil(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> floor(cscalar_t<T> a)
{
    return cscalar_t<T>(floor(a.x), floor(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> round(cscalar_t<T> a)
{
    return cscalar_t<T>(round(a.x), round(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> frac(cscalar_t<T> a)
{
    return cscalar_t<T>(frac(a.x), frac(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> saturate(cscalar_t<T> a)
{
    return cscalar_t<T>(saturate(a.x), saturate(a.y));
}

template <typename T>
INLINE __device__ cscalar_t<T> _float_(cscalar_t<T> a) { return a; }
template <typename T>
INLINE __device__ cscalar_t<T> _int_(cscalar_t<T> a)
{
    return cscalar_t<T>(int(a.x), int(a.y));
}

template <typename T>
INLINE __device__ int _isfinite_(cscalar_t<T> a)
{
    return _isfinite_(a.x) && _isfinite_(a.y);
}
template <typename T>
INLINE __device__ int _isinf_(cscalar_t<T> a)
{
    return _isinf_(a.x) || _isinf_(a.y);
}
template <typename T>
INLINE __device__ int _isnan_(cscalar_t<T> a)
{
    return _isnan_(a.x) || _isnan_(a.y);
}

// Note: originally script-generated. Before CUDA 7, NVCC did not support 
// functions with variadic arguments.
#define DECL_MAKE_T(T)                                                         \
    INLINE T##N<4> __device__ make_##T##N(T a, T b, T c, T d)                  \
    {                                                                          \
        T##N<4> y;                                                             \
        y.el[0] = a;                                                           \
        y.el[1] = b, y.el[2] = c;                                              \
        y.el[3] = d;                                                           \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<5> __device__ make_##T##N(T a, T b, T c, T d, T e)             \
    {                                                                          \
        T##N<5> y = {a, b, c, d, e};                                           \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<6> __device__ make_##T##N(T a, T b, T c, T d, T e, T f)        \
    {                                                                          \
        T##N<6> y = {a, b, c, d, e, f};                                        \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<7> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g)   \
    {                                                                          \
        T##N<7> y = {a, b, c, d, e, f, g};                                     \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<8> __device__                                                  \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h)                    \
    {                                                                          \
        T##N<8> y = {a, b, c, d, e, f, g, h};                                  \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<9> __device__                                                  \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i)               \
    {                                                                          \
        T##N<9> y = {a, b, c, d, e, f, g, h, i};                               \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<10> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j)          \
    {                                                                          \
        T##N<10> y = {a, b, c, d, e, f, g, h, i, j};                           \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<11> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k)     \
    {                                                                          \
        T##N<11> y = {a, b, c, d, e, f, g, h, i, j, k};                        \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<12> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l)            \
    {                                                                          \
        T##N<12> y = {a, b, c, d, e, f, g, h, i, j, k, l};                     \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<13> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m)       \
    {                                                                          \
        T##N<13> y = {a, b, c, d, e, f, g, h, i, j, k, l, m};                  \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<14> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n)  \
    {                                                                          \
        T##N<14> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n};               \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<15> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n,  \
                                           T o)                                \
    {                                                                          \
        T##N<15> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n, o};            \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<16> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n,  \
                                           T o, T p)                           \
    {                                                                          \
        T##N<16> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p};         \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<17> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n,  \
                                           T o, T p, T q)                      \
    {                                                                          \
        T##N<17> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q};      \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<18> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n,  \
                                           T o, T p, T q, T r)                 \
    {                                                                          \
        T##N<18> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r};   \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<19> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n,  \
                                           T o, T p, T q, T r, T s)            \
    {                                                                          \
        T##N<19> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s};\
        return y;                                                              \
    }                                                                          \
    INLINE T##N<20> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n,  \
                                           T o, T p, T q, T r, T s, T t)       \
    {                                                                          \
        T##N<20> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r,    \
						s, t};												   \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<21> __device__ make_##T##N(T a, T b, T c, T d, T e, T f, T g,  \
                                           T h, T i, T j, T k, T l, T m, T n,  \
                                           T o, T p, T q, T r, T s, T t, T u)  \
    {                                                                          \
        T##N<21> y = {                                                         \
            a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u};    \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<22> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k,     \
                    T l, T m, T n, T o, T p, T q, T r, T s, T t, T u, T v)     \
    {                                                                          \
        T##N<22> y = {a, b, c, d, e, f, g, h, i, j, k, l,                      \
                      m, n, o, p, q, r, s, t, u, v};                           \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<23> __device__ make_##T##N(                                    \
        T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k, T l, T m, T n,  \
        T o, T p, T q, T r, T s, T t, T u, T v, T w)                           \
    {                                                                          \
        T##N<23> y = {a, b, c, d, e, f, g, h, i, j, k, l, m,                   \
                      n, o, p, q, r, s, t, u, v, w};                           \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<24> __device__ make_##T##N(                                    \
        T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k, T l, T m, T n,  \
        T o, T p, T q, T r, T s, T t, T u, T v, T w, T x)                      \
    {                                                                          \
        T##N<24> y = {a, b, c, d, e, f, g, h, i, j, k, l, m,                   \
                      n, o, p, q, r, s, t, u, v, w, x};                        \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<25> __device__ make_##T##N(                                    \
        T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k, T l, T m, T n,  \
        T o, T p, T q, T r, T s, T t, T u, T v, T w, T x, T z)                 \
    {                                                                          \
        T##N<25> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n,                \
                      o, p, q, r, s, t, u, v, w, x, z};                        \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<26> __device__ make_##T##N(                                    \
        T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k, T l, T m, T n,  \
        T o, T p, T q, T r, T s, T t, T u, T v, T w, T x, T z, T a1)           \
    {                                                                          \
        T##N<26> y = {a, b, c, d, e, f, g, h, i, j, k, l, m, n,                \
                      o, p, q, r, s, t, u, v, w, x, z, a1};                    \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<27> __device__ make_##T##N(                                    \
        T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k, T l, T m, T n,  \
        T o, T p, T q, T r, T s, T t, T u, T v, T w, T x, T z, T a1, T a2)     \
    {                                                                          \
        T##N<27> y = {a, b, c, d, e, f, g, h, i, j, k, l, m,  n, o,            \
                      p, q, r, s, t, u, v, w, x, z, a1, a2};                   \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<28> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k,     \
                    T l, T m, T n, T o, T p, T q, T r, T s, T t, T u, T v,     \
                    T w, T x, T z, T a1, T a2, T a3)                           \
    {                                                                          \
        T##N<28> y = {a, b, c, d, e, f, g, h, i, j, k, l, m,  n,  o,           \
                      p, q, r, s, t, u, v, w, x, z, a1, a2, a3};               \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<29> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k,     \
                    T l, T m, T n, T o, T p, T q, T r, T s, T t, T u, T v,     \
                    T w, T x, T z, T a1, T a2, T a3, T a4)                     \
    {                                                                          \
        T##N<29> y = {a, b, c, d, e, f, g, h, i, j,  k, l, m,  n,  o, p,       \
                      q, r, s, t, u, v, w, x, z, a1, a2, a3, a4};              \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<30> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k,     \
                    T l, T m, T n, T o, T p, T q, T r, T s, T t, T u, T v,     \
                    T w, T x, T z, T a1, T a2, T a3, T a4, T a5)               \
    {                                                                          \
        T##N<30> y = {a, b, c, d, e, f, g, h, i, j,  k, l, m,  n,  o,  p,      \
                      q, r, s, t, u, v, w, x, z, a1, a2, a3, a4, a5};          \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<31> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k,     \
                    T l, T m, T n, T o, T p, T q, T r, T s, T t, T u, T v,     \
                    T w, T x, T z, T a1, T a2, T a3, T a4, T a5, T a6)         \
    {                                                                          \
        T##N<31> y = {a, b, c, d, e, f, g, h, i,  j,  k, l, m,  n,  o,  p, q,  \
                      r, s, t, u, v, w, x, z, a1, a2, a3, a4, a5, a6};         \
        return y;                                                              \
    }                                                                          \
    INLINE T##N<32> __device__                                                 \
        make_##T##N(T a, T b, T c, T d, T e, T f, T g, T h, T i, T j, T k,     \
                    T l, T m, T n, T o, T p, T q, T r, T s, T t, T u, T v,     \
                    T w, T x, T z, T a1, T a2, T a3, T a4, T a5, T a6, T a7)   \
    {                                                                          \
        T##N<32> y = {a, b, c, d, e, f, g, h, i,  j, k, l,  m,  n,  o,  p,  q, \
                      r, s, t, u, v, w, x, z, a1, a2, a3, a4, a5, a6, a7};     \
        return y;                                                              \
    }


DECL_MAKE_T(int)
DECL_MAKE_T(scalar)
//DECL_MAKE_T(half_t)

// Complex vector types
struct cscalar1
{
    cscalar x;
};
struct cscalar2
{
    cscalar x, y;
};
struct cscalar3
{
    cscalar x, y, z;
};
struct cscalar4
{
    cscalar x, y, z, w;
};
template <int N> struct cscalarN
{
    cscalar el[N];
};

INLINE __device__ cscalar1 make_cscalar1(cscalar x)
{
    cscalar1 t = {x};
    return t;
}
INLINE __device__ cscalar2 make_cscalar2(cscalar x, cscalar y)
{
    cscalar2 t = {x, y};
    return t;
}
INLINE __device__ cscalar3 make_cscalar3(cscalar x, cscalar y, cscalar z)
{
    cscalar3 t = {x, y, z};
    return t;
}
INLINE __device__ cscalar4
make_cscalar4(cscalar x, cscalar y, cscalar z, cscalar w)
{
    cscalar4 t = {x, y, z, w};
    return t;
}

DECL_MAKE_T(cscalar)
#undef DECL_MAKE_T

namespace quasar
{
	#if !defined(HALF_SCALAR)
    typedef VectorBase<cscalar> CVector;
    typedef MatrixBase<cscalar> CMatrix;
    typedef CubeBase<cscalar> CCube;
	#else
    typedef VectorBase<cscalar_t<half_t> > CVector;
    typedef MatrixBase<cscalar_t<half_t> > CMatrix;
    typedef CubeBase<cscalar_t<half_t> > CCube;
	#endif
} // namespace quasar



// helper function for boundary handling
INLINE __device__ int mirror_ext(int n, int N)
{
    n = abs(n) % (2 * N);
    if (n >= N)
        n = 2 * N - 1 - n;
    return n;
}
INLINE __device__ int periodize(int n, int N)
{
    int q = n % N;
    return q < 0 ? q + N : q;
}
INLINE __device__ int clamp(int n, int N) { return max(0, min(N, n)); }
INLINE __device__ scalar mirror_ext(scalar n, scalar N)
{
    n = fmod(abs(n), 2 * N);
    if (n >= N)
        n = 2 * N - 1 - n;
    return n;
}
INLINE __device__ scalar periodize(scalar n, scalar N)
{
    scalar q = fmod(n, N);
    return q < 0 ? q + N : q;
}
INLINE __device__ scalar clamp(scalar n, scalar N)
{
    return max((scalar)0, min((scalar)N, n));
}

//=============================================================================
// UNARY AND BINARY OPERATIONS
//=============================================================================
#define FOR_ALL(X)                                                             \
    for (int i = 0; i < N; i++)                                                \
        (X);
#define DECL_UN_OP_T(X, T)                                                     \
    INLINE T##1 __device__ operator X(T##1 a) { return make_##T##1(X a.x); }   \
    INLINE T##2 __device__ operator X(T##2 a)                                  \
    {                                                                          \
        return make_##T##2(X a.x, X a.y);                                      \
    }                                                                          \
    INLINE T##3 __device__ operator X(T##3 a)                                  \
    {                                                                          \
        return make_##T##3(X a.x, X a.y, X a.z);                               \
    }                                                                          \
    INLINE T##4 __device__ operator X(T##4 a)                                  \
    {                                                                          \
        return make_##T##4(X a.x, X a.y, X a.z, X a.w);                        \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__ operator X(T##N<N> a)           \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X a.el[i]);                                          \
        return y;                                                              \
    }

#define DECL_UN_OP(X)                                                          \
    DECL_UN_OP_T(X, scalar)                                                    \
    DECL_UN_OP_T(X, int)                                                       \
    DECL_UN_OP_T(X, cscalar)

// Scalar/Vector vs. Vector from the same type
#define DECL_BIN_OP_T(X, T)                                                    \
    INLINE T##1 __device__ operator X(T##1 a, T##1 b)                          \
    {                                                                          \
        return make_##T##1(a.x X b.x);                                         \
    }                                                                          \
    INLINE T##2 __device__ operator X(T##2 a, T##2 b)                          \
    {                                                                          \
        return make_##T##2(a.x X b.x, a.y X b.y);                              \
    }                                                                          \
    INLINE T##3 __device__ operator X(T##3 a, T##3 b)                          \
    {                                                                          \
        return make_##T##3(a.x X b.x, a.y X b.y, a.z X b.z);                   \
    }                                                                          \
    INLINE T##4 __device__ operator X(T##4 a, T##4 b)                          \
    {                                                                          \
        return make_##T##4(a.x X b.x, a.y X b.y, a.z X b.z, a.w X b.w);        \
    }                                                                          \
    INLINE T##1 __device__ operator X(T a, T##1 b)                             \
    {                                                                          \
        return make_##T##1(a X b.x);                                           \
    }                                                                          \
    INLINE T##2 __device__ operator X(T a, T##2 b)                             \
    {                                                                          \
        return make_##T##2(a X b.x, a X b.y);                                  \
    }                                                                          \
    INLINE T##3 __device__ operator X(T a, T##3 b)                             \
    {                                                                          \
        return make_##T##3(a X b.x, a X b.y, a X b.z);                         \
    }                                                                          \
    INLINE T##4 __device__ operator X(T a, T##4 b)                             \
    {                                                                          \
        return make_##T##4(a X b.x, a X b.y, a X b.z, a X b.w);                \
    }                                                                          \
    INLINE T##1 __device__ operator X(T##1 a, T b)                             \
    {                                                                          \
        return make_##T##1(a.x X b);                                           \
    }                                                                          \
    INLINE T##2 __device__ operator X(T##2 a, T b)                             \
    {                                                                          \
        return make_##T##2(a.x X b, a.y X b);                                  \
    }                                                                          \
    INLINE T##3 __device__ operator X(T##3 a, T b)                             \
    {                                                                          \
        return make_##T##3(a.x X b, a.y X b, a.z X b);                         \
    }                                                                          \
    INLINE T##4 __device__ operator X(T##4 a, T b)                             \
    {                                                                          \
        return make_##T##4(a.x X b, a.y X b, a.z X b, a.w X b);                \
    }                                                                          \
    template <int N>                                                           \
    INLINE T##N<N> __device__ operator X(T##N<N> a, T##N<N> b)                 \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b.el[i]);                                  \
        return y;                                                              \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__ operator X(T a, T##N<N> b)      \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a X b.el[i]);                                        \
        return y;                                                              \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__ operator X(T##N<N> a, T b)      \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b);                                        \
        return y;                                                              \
    }

// Scalar vs Vector from different types
#define DECL_BIN_OP_MIXT1(X, I, R)                                             \
    INLINE R##1 __device__ operator X(I a, R##1 b)                             \
    {                                                                          \
        return make_##R##1(a X b.x);                                           \
    }                                                                          \
    INLINE R##2 __device__ operator X(I a, R##2 b)                             \
    {                                                                          \
        return make_##R##2(a X b.x, a X b.y);                                  \
    }                                                                          \
    INLINE R##3 __device__ operator X(I a, R##3 b)                             \
    {                                                                          \
        return make_##R##3(a X b.x, a X b.y, a X b.z);                         \
    }                                                                          \
    INLINE R##4 __device__ operator X(I a, R##4 b)                             \
    {                                                                          \
        return make_##R##4(a X b.x, a X b.y, a X b.z, a X b.w);                \
    }                                                                          \
    INLINE R##1 __device__ operator X(R##1 a, I b)                             \
    {                                                                          \
        return make_##R##1(a.x X b);                                           \
    }                                                                          \
    INLINE R##2 __device__ operator X(R##2 a, I b)                             \
    {                                                                          \
        return make_##R##2(a.x X b, a.y X b);                                  \
    }                                                                          \
    INLINE R##3 __device__ operator X(R##3 a, I b)                             \
    {                                                                          \
        return make_##R##3(a.x X b, a.y X b, a.z X b);                         \
    }                                                                          \
    INLINE R##4 __device__ operator X(R##4 a, I b)                             \
    {                                                                          \
        return make_##R##4(a.x X b, a.y X b, a.z X b, a.w X b);                \
    }                                                                          \
    template <int N> INLINE R##N<N> __device__ operator X(R##N<N> a, I b)      \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b);                                        \
        return y;                                                              \
    }                                                                          \
    template <int N> INLINE R##N<N> __device__ operator X(I a, R##N<N> b)      \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a X b.el[i]);                                        \
        return y;                                                              \
    }                                                                          \
    INLINE R##1 __device__ operator X(I##1 a, R b)                             \
    {                                                                          \
        return make_##R##1(a.x X b);                                           \
    }                                                                          \
    INLINE R##2 __device__ operator X(I##2 a, R b)                             \
    {                                                                          \
        return make_##R##2(a.x X b, a.y X b);                                  \
    }                                                                          \
    INLINE R##3 __device__ operator X(I##3 a, R b)                             \
    {                                                                          \
        return make_##R##3(a.x X b, a.y X b, a.z X b);                         \
    }                                                                          \
    INLINE R##4 __device__ operator X(I##4 a, R b)                             \
    {                                                                          \
        return make_##R##4(a.x X b, a.y X b, a.z X b, a.w X b);                \
    }                                                                          \
    INLINE R##1 __device__ operator X(R a, I##1 b)                             \
    {                                                                          \
        return make_##R##1(a X b.x);                                           \
    }                                                                          \
    INLINE R##2 __device__ operator X(R a, I##2 b)                             \
    {                                                                          \
        return make_##R##2(a X b.x, a X b.y);                                  \
    }                                                                          \
    INLINE R##3 __device__ operator X(R a, I##3 b)                             \
    {                                                                          \
        return make_##R##3(a X b.x, a X b.y, a X b.z);                         \
    }                                                                          \
    INLINE R##4 __device__ operator X(R a, I##4 b)                             \
    {                                                                          \
        return make_##R##4(a X b.x, a X b.y, a X b.z, a X b.w);                \
    }                                                                          \
    template <int N> INLINE R##N<N> __device__ operator X(R a, I##N<N> b)      \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a X b.el[i]);                                        \
        return y;                                                              \
    }                                                                          \
    template <int N> INLINE R##N<N> __device__ operator X(I##N<N> a, R b)      \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b);                                        \
        return y;                                                              \
    }

// Vector vs Vector from different types
#define DECL_BIN_OP_MIXT(X, I, R)                                              \
    INLINE R##1 __device__ operator X(I##1 a, R##1 b)                          \
    {                                                                          \
        return make_##R##1(a.x X b.x);                                         \
    }                                                                          \
    INLINE R##2 __device__ operator X(I##2 a, R##2 b)                          \
    {                                                                          \
        return make_##R##2(a.x X b.x, a.y X b.y);                              \
    }                                                                          \
    INLINE R##3 __device__ operator X(I##3 a, R##3 b)                          \
    {                                                                          \
        return make_##R##3(a.x X b.x, a.y X b.y, a.z X b.z);                   \
    }                                                                          \
    INLINE R##4 __device__ operator X(I##4 a, R##4 b)                          \
    {                                                                          \
        return make_##R##4(a.x X b.x, a.y X b.y, a.z X b.z, a.w X b.w);        \
    }                                                                          \
    INLINE R##1 __device__ operator X(R##1 a, I##1 b)                          \
    {                                                                          \
        return make_##R##1(a.x X b.x);                                         \
    }                                                                          \
    INLINE R##2 __device__ operator X(R##2 a, I##2 b)                          \
    {                                                                          \
        return make_##R##2(a.x X b.x, a.y X b.y);                              \
    }                                                                          \
    INLINE R##3 __device__ operator X(R##3 a, I##3 b)                          \
    {                                                                          \
        return make_##R##3(a.x X b.x, a.y X b.y, a.z X b.z);                   \
    }                                                                          \
    INLINE R##4 __device__ operator X(R##4 a, I##4 b)                          \
    {                                                                          \
        return make_##R##4(a.x X b.x, a.y X b.y, a.z X b.z, a.w X b.w);        \
    }                                                                          \
    template <int N>                                                           \
    INLINE R##N<N> __device__ operator X(R##N<N> a, I##N<N> b)                 \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b.el[i]);                                  \
        return y;                                                              \
    }                                                                          \
    template <int N>                                                           \
    INLINE R##N<N> __device__ operator X(I##N<N> a, R##N<N> b)                 \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b.el[i]);                                  \
        return y;                                                              \
    }


#define DECL_BIN_OP(X)                                                         \
    DECL_BIN_OP_T(X, scalar)                                                   \
    DECL_BIN_OP_T(X, int)                                                      \
    DECL_BIN_OP_T(X, cscalar)                                                  \
    DECL_BIN_OP_MIXT(X, int, scalar)                                           \
    DECL_BIN_OP_MIXT(X, scalar, cscalar)                                       \
    DECL_BIN_OP_MIXT1(X, int, scalar)                                          \
    DECL_BIN_OP_MIXT1(X, scalar, cscalar)

#define DECL_BIN_OP_REAL(X)                                                    \
    DECL_BIN_OP_T(X, scalar)                                                   \
    DECL_BIN_OP_T(X, int)                                                      \
    DECL_BIN_OP_MIXT(X, int, scalar)

#define DECL_INPLACE_OP_T(X, T)                                                \
    INLINE T##1 __device__ operator X(T##1 & a, T##1 b)                        \
    {                                                                          \
        return make_##T##1(a.x X b.x);                                         \
    }                                                                          \
    INLINE T##2 __device__ operator X(T##2 & a, T##2 b)                        \
    {                                                                          \
        return make_##T##2(a.x X b.x, a.y X b.y);                              \
    }                                                                          \
    INLINE T##3 __device__ operator X(T##3 & a, T##3 b)                        \
    {                                                                          \
        return make_##T##3(a.x X b.x, a.y X b.y, a.z X b.z);                   \
    }                                                                          \
    INLINE T##4 __device__ operator X(T##4 & a, T##4 b)                        \
    {                                                                          \
        return make_##T##4(a.x X b.x, a.y X b.y, a.z X b.z, a.w X b.w);        \
    }                                                                          \
    INLINE T##1 __device__ operator X(T##1 & a, T b)                           \
    {                                                                          \
        return make_##T##1(a.x X b);                                           \
    }                                                                          \
    INLINE T##2 __device__ operator X(T##2 & a, T b)                           \
    {                                                                          \
        return make_##T##2(a.x X b, a.y X b);                                  \
    }                                                                          \
    INLINE T##3 __device__ operator X(T##3 & a, T b)                           \
    {                                                                          \
        return make_##T##3(a.x X b, a.y X b, a.z X b);                         \
    }                                                                          \
    INLINE T##4 __device__ operator X(T##4 & a, T b)                           \
    {                                                                          \
        return make_##T##4(a.x X b, a.y X b, a.z X b, a.w X b);                \
    }                                                                          \
    template <int N>                                                           \
    INLINE T##N<N> __device__ operator X(T##N<N> &a, T##N<N> b)                \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b.el[i]);                                  \
        return y;                                                              \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__ operator X(T##N<N> &a, T b)     \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = a.el[i] X b);                                        \
        return y;                                                              \
    }

#define DECL_INPLACE_OP(X)                                                     \
    DECL_INPLACE_OP_T(X, scalar)                                               \
    DECL_INPLACE_OP_T(X, int)                                                  \
    DECL_INPLACE_OP_T(X, cscalar)

DECL_UN_OP(-)
DECL_BIN_OP(+)
DECL_BIN_OP(-)
DECL_BIN_OP(*)
DECL_BIN_OP(/ )
DECL_INPLACE_OP(-= )
DECL_INPLACE_OP(+= )
DECL_INPLACE_OP(*= )
DECL_INPLACE_OP(/= )

// Comparison operators : only scalar and int!
DECL_BIN_OP_REAL(> )
DECL_BIN_OP_REAL(< )
DECL_BIN_OP_REAL(>= )
DECL_BIN_OP_REAL(<= )
DECL_BIN_OP_REAL(== )
DECL_BIN_OP_REAL(!= )

#undef DECL_UN_OP_T
#undef DECL_UN_OP
#undef DECL_BIN_OP_T
#undef DECL_BIN_OP
#undef DECL_BIN_OP_REAL
#undef DECL_INPLACE_OP

//=============================================================================
// FUNCTION DEFINITIONS OF VECTOR TYPES
// Note: the parentheses around X are added to prevent macro expansion!
//=============================================================================
#define UNARY_FUNCTION_T(X, T)                                                 \
    INLINE T##1 __device__(X)(T##1 a) { return make_##T##1(X(a.x)); }          \
    INLINE T##2 __device__(X)(T##2 a) { return make_##T##2(X(a.x), X(a.y)); }  \
    INLINE T##3 __device__(X)(T##3 a)                                          \
    {                                                                          \
        return make_##T##3(X(a.x), X(a.y), X(a.z));                            \
    }                                                                          \
    INLINE T##4 __device__(X)(T##4 a)                                          \
    {                                                                          \
        return make_##T##4(X(a.x), X(a.y), X(a.z), X(a.w));                    \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__(X)(T##N<N> a)                   \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X(a.el[i]));                                         \
        return y;                                                              \
    }
#define UNARY_FUNCTION_TR(X, T, R)                                             \
    INLINE R##1 __device__(X)(T##1 a) { return make_##R##1(X(a.x)); }          \
    INLINE R##2 __device__(X)(T##2 a) { return make_##R##2(X(a.x), X(a.y)); }  \
    INLINE R##3 __device__(X)(T##3 a)                                          \
    {                                                                          \
        return make_##R##3(X(a.x), X(a.y), X(a.z));                            \
    }                                                                          \
    INLINE R##4 __device__(X)(T##4 a)                                          \
    {                                                                          \
        return make_##R##4(X(a.x), X(a.y), X(a.z), X(a.w));                    \
    }                                                                          \
    template <int N> INLINE R##N<N> __device__(X)(T##N<N> a)                   \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X(a.el[i]));                                         \
        return y;                                                              \
    }
#define BINARY_FUNCTION_T(X, T)                                                \
    INLINE T##1 __device__(X)(T##1 a, T##1 b)                                  \
    {                                                                          \
        return make_##T##1(X(a.x, b.x));                                       \
    }                                                                          \
    INLINE T##2 __device__(X)(T##2 a, T##2 b)                                  \
    {                                                                          \
        return make_##T##2(X(a.x, b.x), X(a.y, b.y));                          \
    }                                                                          \
    INLINE T##3 __device__(X)(T##3 a, T##3 b)                                  \
    {                                                                          \
        return make_##T##3(X(a.x, b.x), X(a.y, b.y), X(a.z, b.z));             \
    }                                                                          \
    INLINE T##4 __device__(X)(T##4 a, T##4 b)                                  \
    {                                                                          \
        return make_##T##4(X(a.x, b.x), X(a.y, b.y), X(a.z, b.z),              \
                           X(a.w, b.w));                                       \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__(X)(T##N<N> a, T##N<N> b)        \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X(a.el[i], b.el[i]));                                \
        return y;                                                              \
    }
#define BINARY_FUNCTION_TR(X, T, A, B)                                         \
    INLINE T##1 __device__(X)(A##1 a, B##1 b)                                  \
    {                                                                          \
        return make_##T##1(X(a.x, b.x));                                       \
    }                                                                          \
    INLINE T##2 __device__(X)(A##2 a, B##2 b)                                  \
    {                                                                          \
        return make_##T##2(X(a.x, b.x), X(a.y, b.y));                          \
    }                                                                          \
    INLINE T##3 __device__(X)(A##3 a, B##3 b)                                  \
    {                                                                          \
        return make_##T##3(X(a.x, b.x), X(a.y, b.y), X(a.z, b.z));             \
    }                                                                          \
    INLINE T##4 __device__(X)(A##4 a, B##4 b)                                  \
    {                                                                          \
        return make_##T##4(X(a.x, b.x), X(a.y, b.y), X(a.z, b.z),              \
                           X(a.w, b.w));                                       \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__(X)(A##N<N> a, B##N<N> b)        \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X(a.el[i], b.el[i]));                                \
        return y;                                                              \
    }
#define BINARY_FUNCTION_TR2(X, R, T)                                           \
    INLINE R##1 __device__(X)(T a, T##1 b) { return make_##R##1(X(a, b.x)); }  \
    INLINE R##2 __device__(X)(T a, T##2 b)                                     \
    {                                                                          \
        return make_##R##2(X(a, b.x), X(a, b.y));                              \
    }                                                                          \
    INLINE R##3 __device__(X)(T a, T##3 b)                                     \
    {                                                                          \
        return make_##R##3(X(a, b.x), X(a, b.y), X(a, b.z));                   \
    }                                                                          \
    INLINE R##4 __device__(X)(T a, T##4 b)                                     \
    {                                                                          \
        return make_##R##4(X(a, b.x), X(a, b.y), X(a, b.z), X(a, b.w));        \
    }                                                                          \
    INLINE R##1 __device__(X)(T##1 a, T b) { return make_##R##1(X(a.x, b)); }  \
    INLINE R##2 __device__(X)(T##2 a, T b)                                     \
    {                                                                          \
        return make_##R##2(X(a.x, b), X(a.y, b));                              \
    }                                                                          \
    INLINE R##3 __device__(X)(T##3 a, T b)                                     \
    {                                                                          \
        return make_##R##3(X(a.x, b), X(a.y, b), X(a.z, b));                   \
    }                                                                          \
    INLINE R##4 __device__(X)(T##4 a, T b)                                     \
    {                                                                          \
        return make_##R##4(X(a.x, b), X(a.y, b), X(a.z, b), X(a.w, b));        \
    }                                                                          \
    template <int N> INLINE R##N<N> __device__(X)(T a, T##N<N> b)              \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X(a, b.el[i]));                                      \
        return y;                                                              \
    }                                                                          \
    template <int N> INLINE R##N<N> __device__(X)(T##N<N> a, T b)              \
    {                                                                          \
        R##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X(a.el[i], b));                                      \
        return y;                                                              \
    }


// Integer/scalar functions
UNARY_FUNCTION_T(abs, scalar);
UNARY_FUNCTION_T(acos, scalar);
UNARY_FUNCTION_T(atan, scalar);
BINARY_FUNCTION_T(atan2, scalar);
UNARY_FUNCTION_T(conj, scalar);
UNARY_FUNCTION_T(ceil, scalar);
UNARY_FUNCTION_T(round, scalar);
UNARY_FUNCTION_T(cos, scalar);
UNARY_FUNCTION_T(sin, scalar);
UNARY_FUNCTION_T(exp, scalar);
UNARY_FUNCTION_T(exp2, scalar);
UNARY_FUNCTION_T(floor, scalar);
BINARY_FUNCTION_T(mod, scalar);
UNARY_FUNCTION_T(frac, scalar);
UNARY_FUNCTION_T(log, scalar);
UNARY_FUNCTION_T(log2, scalar);
UNARY_FUNCTION_T(log10, scalar);
BINARY_FUNCTION_T(max, scalar);
BINARY_FUNCTION_T(min, scalar);
BINARY_FUNCTION_T(pow, scalar);
BINARY_FUNCTION_TR2(pow, scalar, scalar);
UNARY_FUNCTION_T(saturate, scalar);
UNARY_FUNCTION_T(sign, scalar);
UNARY_FUNCTION_T(sqrt, scalar);
UNARY_FUNCTION_T(rsqrt, scalar);
UNARY_FUNCTION_T(sinh, scalar);
UNARY_FUNCTION_T(cosh, scalar);
UNARY_FUNCTION_T(tanh, scalar);
#ifdef _MSC_VER
UNARY_FUNCTION_T(asinh, scalar);
UNARY_FUNCTION_T(acosh, scalar);
UNARY_FUNCTION_T(atanh, scalar);
#endif
UNARY_FUNCTION_T(erf, scalar);
UNARY_FUNCTION_T(erfc, scalar);
UNARY_FUNCTION_T(erfinv, scalar);
UNARY_FUNCTION_T(erfcinv, scalar);
UNARY_FUNCTION_T(lgamma, scalar);
UNARY_FUNCTION_T(tgamma, scalar);
UNARY_FUNCTION_TR(_isnan_, scalar, int);
UNARY_FUNCTION_TR(_isinf_, scalar, int);
UNARY_FUNCTION_TR(_isfinite_, scalar, int);
BINARY_FUNCTION_T(periodize, scalar);
BINARY_FUNCTION_T(mirror_ext, scalar);
BINARY_FUNCTION_T(clamp, scalar);

// Integer functions
UNARY_FUNCTION_T(abs, int);
BINARY_FUNCTION_T(mod, int);
BINARY_FUNCTION_T(_and_, int);
BINARY_FUNCTION_T(_or_, int);
BINARY_FUNCTION_T(_xor_, int);
UNARY_FUNCTION_T(_not_, int);
BINARY_FUNCTION_T(_shl_, int);
BINARY_FUNCTION_T(_shr_, int);
UNARY_FUNCTION_T(sign, int);
BINARY_FUNCTION_T(min, int);
BINARY_FUNCTION_T(max, int);
BINARY_FUNCTION_T(periodize, int);
BINARY_FUNCTION_T(mirror_ext, int);
BINARY_FUNCTION_T(clamp, int);
UNARY_FUNCTION_T(_isnan_, int);
UNARY_FUNCTION_T(_isinf_, int);
UNARY_FUNCTION_T(_isfinite_, int);
BINARY_FUNCTION_TR(sdiv, scalar, int, int);
BINARY_FUNCTION_TR2(sdiv, scalar, int);
BINARY_FUNCTION_T(idiv, int);
BINARY_FUNCTION_TR2(idiv, int, int);
BINARY_FUNCTION_TR2(pow, int, int);


// Complex functions
UNARY_FUNCTION_TR(abs, cscalar, scalar);
UNARY_FUNCTION_TR(real, cscalar, scalar);
UNARY_FUNCTION_TR(imag, cscalar, scalar);
UNARY_FUNCTION_TR(angle, cscalar, scalar);
UNARY_FUNCTION_T(conj, cscalar);
BINARY_FUNCTION_T(pow, cscalar);
BINARY_FUNCTION_TR(pow, cscalar, cscalar, scalar);
BINARY_FUNCTION_TR(pow, cscalar, scalar, cscalar);
UNARY_FUNCTION_T(log, cscalar);
UNARY_FUNCTION_T(log10, cscalar);
UNARY_FUNCTION_T(log2, cscalar);
UNARY_FUNCTION_T(sin, cscalar);
UNARY_FUNCTION_T(cos, cscalar);
UNARY_FUNCTION_T(tan, cscalar);
UNARY_FUNCTION_T(exp, cscalar);
UNARY_FUNCTION_TR(sign, cscalar, scalar);
UNARY_FUNCTION_T(sqrt, cscalar);
UNARY_FUNCTION_TR(_isnan_, cscalar, int);
UNARY_FUNCTION_TR(_isinf_, cscalar, int);
UNARY_FUNCTION_TR(_isfinite_, cscalar, int);


// Misc functions
INLINE scalar __device__ min(scalar2 a) { return min(a.x, a.y); }
INLINE scalar __device__ min(scalar3 a) { return min(a.x, min(a.y, a.z)); }
INLINE scalar __device__ min(scalar4 a)
{
    return min(min(a.x, a.y), min(a.z, a.w));
}
INLINE scalar __device__ max(scalar2 a) { return max(a.x, a.y); }
INLINE scalar __device__ max(scalar3 a) { return max(a.x, max(a.y, a.z)); }
INLINE scalar __device__ max(scalar4 a)
{
    return max(max(a.x, a.y), max(a.z, a.w));
}
template <int N> scalar __device__ min(scalarN<N> a)
{
    scalar y = a.el[0];
    for (int i = 1; i < N; i++)
        y = min(y, a.el[i]);
    return y;
}
template <int N> scalar __device__ max(scalarN<N> a)
{
    scalar y = a.el[0];
    for (int i = 1; i < N; i++)
        y = max(y, a.el[i]);
    return y;
}

INLINE int __device__ min(int2 a) { return min(a.x, a.y); }
INLINE int __device__ min(int3 a) { return min(a.x, min(a.y, a.z)); }
INLINE int __device__ min(int4 a) { return min(min(a.x, a.y), min(a.z, a.w)); }
INLINE int __device__ max(int2 a) { return max(a.x, a.y); }
INLINE int __device__ max(int3 a) { return max(a.x, max(a.y, a.z)); }
INLINE int __device__ max(int4 a) { return max(max(a.x, a.y), max(a.z, a.w)); }
template <int N> int __device__ min(intN<N> a)
{
    scalar y = a.el[0];
    for (int i = 1; i < N; i++)
        y = min(y, a.el[i]);
    return y;
}
template <int N> int __device__ max(intN<N> a)
{
    scalar y = a.el[0];
    for (int i = 1; i < N; i++)
        y = max(y, a.el[i]);
    return y;
}

//=============================================================================
// TYPE CASTING OPERATIONS
//=============================================================================
INLINE scalar __device__ __cast_scalar(int a) { return (scalar)a; }
INLINE scalar __device__ __cast_scalar(scalar a) { return (scalar)a; }
INLINE int __device__ __cast_int(scalar a) { return (int)a; }
INLINE int __device__ __cast_int(int a) { return (int)a; }
INLINE cscalar __device__ __cast_complex(scalar a) { return cscalar(a); }
INLINE cscalar __device__ __cast_complex(cscalar a) { return cscalar(a); }
UNARY_FUNCTION_TR(__cast_scalar, int, scalar);
UNARY_FUNCTION_TR(__cast_int, scalar, int);
UNARY_FUNCTION_TR(__cast_complex, scalar, cscalar);
UNARY_FUNCTION_T(__cast_scalar, scalar);
UNARY_FUNCTION_T(__cast_int, int);
UNARY_FUNCTION_T(__cast_complex, cscalar);

#define FUNCTION_CAST_COMPLEX_DEF(T)                                           \
    INLINE cscalar __device__ __cast_complex(T a, T b)                         \
    {                                                                          \
        return make_cscalar(a, b);                                             \
    }                                                                          \
    INLINE cscalar1 __device__ __cast_complex(T##1 a, T##1 b)                  \
    {                                                                          \
        return make_cscalar1(cscalar(a.x, b.x));                               \
    }                                                                          \
    INLINE cscalar2 __device__ __cast_complex(T##2 a, T##2 b)                  \
    {                                                                          \
        return make_cscalar2(cscalar(a.x, b.x), cscalar(a.y, b.y));            \
    }                                                                          \
    INLINE cscalar3 __device__ __cast_complex(T##3 a, T##3 b)                  \
    {                                                                          \
        return make_cscalar3(cscalar(a.x, b.x), cscalar(a.y, b.y),             \
                             cscalar(a.z, b.z));                               \
    }                                                                          \
    INLINE cscalar4 __device__ __cast_complex(T##4 a, T##4 b)                  \
    {                                                                          \
        return make_cscalar4(cscalar(a.x, b.x), cscalar(a.y, b.y),             \
                             cscalar(a.z, b.z), cscalar(a.w, b.w));            \
    }                                                                          \
    template <int N>                                                           \
    cscalarN<N> __device__ __cast_complex(T##N<N> a, T##N<N> b)                \
    {                                                                          \
        cscalarN<N> t;                                                         \
        for (int i = 0; i < N; i++)                                            \
            t.el[i] = cscalar(a.el[i], b.el[i]);                               \
        return t;                                                              \
    }
#define FUNCTION_DOTPROD_DEF(T)                                                \
    INLINE T __device__ dotprod(T a, T b) { return a * b; }                    \
    INLINE T __device__ dotprod(T##1 a, T##1 b) { return a.x * b.x; }          \
    INLINE T __device__ dotprod(T##2 a, T##2 b)                                \
    {                                                                          \
        return a.x * b.x + a.y * b.y;                                          \
    }                                                                          \
    INLINE T __device__ dotprod(T##3 a, T##3 b)                                \
    {                                                                          \
        return a.x * b.x + a.y * b.y + a.z * b.z;                              \
    }                                                                          \
    INLINE T __device__ dotprod(T##4 a, T##4 b)                                \
    {                                                                          \
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;                  \
    }                                                                          \
    template <int N> T __device__ dotprod(T##N<N> a, T##N<N> b)                \
    {                                                                          \
        T t = T(0);                                                            \
        FOR_ALL(t += a.el[i] * b.el[i]);                                       \
        return t;                                                              \
    }
#define FUNCTION_LERP_DEF(T)                                                   \
    INLINE T __device__ lerp(T a, T b, T d) { return a * (1 - d) + b * d; }    \
    INLINE T##1 __device__ lerp(T##1 a, T##1 b, T d)                           \
    {                                                                          \
        return a * (1 - d) + b * d;                                            \
    }                                                                          \
    INLINE T##2 __device__ lerp(T##2 a, T##2 b, T d)                           \
    {                                                                          \
        return a * (1 - d) + b * d;                                            \
    }                                                                          \
    INLINE T##3 __device__ lerp(T##3 a, T##3 b, T d)                           \
    {                                                                          \
        return a * (1 - d) + b * d;                                            \
    }                                                                          \
    INLINE T##4 __device__ lerp(T##4 a, T##4 b, T d)                           \
    {                                                                          \
        return a * (1 - d) + b * d;                                            \
    }                                                                          \
    template <int N> T __device__ lerp(T##N<N> a, T##N<N> b, T d)              \
    {                                                                          \
        return a * (1 - d) + b * d;                                            \
    }

FUNCTION_CAST_COMPLEX_DEF(scalar);

FUNCTION_DOTPROD_DEF(int);
FUNCTION_DOTPROD_DEF(scalar);
FUNCTION_DOTPROD_DEF(cscalar);

FUNCTION_LERP_DEF(scalar);
FUNCTION_LERP_DEF(cscalar);

#undef UNARY_FUNCTION_T
#undef UNARY_FUNCTION_TR
#undef BINARY_FUNCTION_T
#undef BINARY_FUNCTION_TR
#undef BINARY_FUNCTION_TR2
#undef FUNCTION_CAST_COMPLEX_DEF
#undef FUNCTION_DOTPROD_DEF
#undef FUNCTION_LERP_DEF

//=============================================================================
//		ERRORS & ERROR HANDLING
//=============================================================================
#define ERRORCODE_OUTOFBOUNDS 1
#define ERRORCODE_OVERFLOW 2
#define ERRORCODE_USERERROR 3
#define ERRORCODE_NAN_OR_INF 4
#define ERRORCODE_NAN 5
#define ERRORCODE_ASSERTIONFAILED 6
#define ERRORCODE_DYNMEM_BLOCKSIZE_TOOLARGE 7
#define ERRORCODE_DYNMEM_OUTOFMEMORY 8
#define ERRORCODE_DYNMEM_INVALIDSTATE 9

struct _ERRORARGS
{
    const void *target;
    int target_bytes;
    int code;
};

#if !defined(TARGET_CUDA)
extern "C" {
#ifdef __QUASAR_NO_EXPORTS__
EXPORT extern struct _ERRORARGS _error;
#else
EXPORT SELECTANY struct _ERRORARGS _error = {};
#endif // __QUASAR_NO_EXPORTS__
}
#else
__device__ struct _ERRORARGS _error;
#endif

namespace quasar
{

    INLINE __device__ void raise_error(const char *msg, int len)
    {
        _error.target = msg;
        _error.target_bytes = len;
        _error.code = ERRORCODE_USERERROR;
#if defined(_MSC_VER) && !defined(TARGET_CUDA)
        throw msg; // Exception will be captured by a surrounding try/catch
                   // block
#endif
    }

#define Q_RAISE_ERROR(msg) raise_error(msg, sizeof(msg))
}

//=============================================================================
// Minimum and maximum values of primitive data types
//=============================================================================

#define MINVAL_uint8 0
#define MAXVAL_uint8 UCHAR_MAX
#define MINVAL_uint16 0
#define MAXVAL_uint16 USHRT_MAX
#define MINVAL_uint32 0
#define MAXVAL_uint32 UINT_MAX
#define MINVAL_uint64 0
#define MAXVAL_uint64 ULLONG_MAX
#define MINVAL_int8 SCHAR_MIN
#define MAXVAL_int8 SCHAR_MAX
#define MINVAL_int16 SHRT_MIN
#define MAXVAL_int16 SHRT_MAX
#define MINVAL_int32 INT_MIN
#define MAXVAL_int32 INT_MAX
#define MINVAL_int64 LLONG_MIN
#define MAXVAL_int64 LLONG_MAX

#ifdef DBL_SCALAR
#define MINVAL_scalar DBL_MIN
#define MAXVAL_scalar DBL_MAX
#else
#define MINVAL_scalar FLT_MIN
#define MAXVAL_scalar FLT_MAX
#endif
#define MINVAL_cscalar make_cscalar(MINVAL_scalar,MINVAL_scalar)
#define MAXVAL_cscalar make_cscalar(MAXVAL_scalar,MAXVAL_scalar)

//=============================================================================
// INTEGER CONVERSION FUNCTIONS
// Unfortunately, CUDA 4.2 (in Linux) cannot deal correctly with template
// constants, so we need to do this in the old-fashioned way.
//=============================================================================

#define DECL_ICVT(T, R, name)                                                  \
    INLINE R##_t __device__ name##_##T##_##R(T val)                            \
    {                                                                          \
        return name<T, R##_t>(val, MINVAL_##R, MAXVAL_##R);                    \
    }
#define DECL_ICVT2(T, R, name)                                                 \
    INLINE R __device__ name##_##T##_##R(T val)                                \
    {                                                                          \
        return name<T, R>(val, MINVAL_##R, MAXVAL_##R);                        \
    }
#define DECL_ICVT_TYPES(name)                                                  \
    DECL_ICVT(scalar, int8, name)                                              \
    DECL_ICVT(scalar, int16, name)                                             \
    DECL_ICVT(scalar, int32, name)                                             \
    DECL_ICVT(scalar, int64, name)                                             \
    DECL_ICVT(scalar, uint8, name)                                             \
    DECL_ICVT(scalar, uint16, name)                                            \
    DECL_ICVT(scalar, uint32, name)                                            \
    DECL_ICVT(scalar, uint64, name)                                            \
    DECL_ICVT(int, int8, name)                                                 \
    DECL_ICVT(int, int16, name)                                                \
    DECL_ICVT(int, int32, name)                                                \
    DECL_ICVT(int, int64, name)                                                \
    DECL_ICVT(int, uint8, name)                                                \
    DECL_ICVT(int, uint16, name)                                               \
    DECL_ICVT(int, uint32, name)                                               \
    DECL_ICVT(int, uint64, name)                                               \
    INLINE int __device__ name##_scalar_int(scalar val)                        \
    {                                                                          \
        return name<scalar, int>(val, MINVAL_int32, MAXVAL_int32);             \
    }

// Integer conversion, unchecked
template <typename T, typename R>
INLINE R __device__ _icvtunchecked(T val, R minval, R maxval)
{
    return (R)val;
}

// Integer conversion, with saturation from T->R
template <typename T, typename R>
INLINE R __device__ _icvtsat(T val, R minval, R maxval)
{
    if (val < (T)minval)
        return minval;
    else if (val > (T)maxval)
        return maxval;
    else
        return (R)val;
}

// Integer conversion, checked
template <typename T, typename R>
INLINE R __device__ _icvtchecked(T val, R minval, R maxval)
{
    if (val < (T)minval || val > (T)maxval)
    {
        _error.target = &val; // TODO!
        _error.target_bytes = sizeof(void *);
        _error.code = ERRORCODE_OVERFLOW;
        val = (T)0; // Set to zero
        assert(0);
    }
    return (R)val;
}

DECL_ICVT_TYPES(_icvtunchecked)
DECL_ICVT_TYPES(_icvtsat)
DECL_ICVT_TYPES(_icvtchecked)

#undef DECL_ICVT
#undef DECL_ICVT_TYPES

//=============================================================================
// OTHER CONVERSION FUNCTIONS
//=============================================================================

#define DECL_CVT(T, R, name)													\
	INLINE __device__ R name(T a) { return R(a); }								\
	INLINE __device__ R##1 name(T##1 a)											\
	{																			\
		return make_##R##1(R(a.x));												\
	}																			\
	INLINE __device__ R##2 name(T##2 a)											\
	{																			\
		return make_##R##2(R(a.x), R(a.y));										\
	}																			\
	INLINE __device__ R##3 name(T##3 a)											\
	{																			\
		return make_##R##3(R(a.x), R(a.y), R(a.z));								\
	}																			\
	INLINE __device__ R##4 name(T##4 a)											\
	{																			\
		return make_##R##4(R(a.x), R(a.y), R(a.z), R(a.w));						\
	}																			

#define DECL_CVTN(T, R, name)													\
	DECL_CVT(T, R, name)														\
	template <int N> INLINE __device__ R##N<N> name(T##N<N> a)					\
	{																			\
		R##N<N> y;																\
		for (int i = 0; i < N; i++)												\
			y.el[i] = R(a.el[i]);												\
		return y;																\
	}

DECL_CVTN(int, scalar, cvt_to_scalar)
DECL_CVTN(scalar, cscalar, cvt_to_complex)
DECL_CVTN(int, cscalar, cvt_to_complex)

// Conversion functions used for hardware texturing
#ifdef TARGET_CUDA
DECL_CVT(float,  scalar, cvt_to_scalar)
DECL_CVT(double, scalar, cvt_to_scalar)
DECL_CVT(scalar, float, cvt_to_float)
DECL_CVT(scalar, half_t, cvt_to_half)
#endif

#ifndef TARGET_CUDA
INLINE __device__ scalar cvt_to_scalar(scalar x) { return x; }
#endif
INLINE __device__ scalar cvt_to_scalar(uint32_t x) { return scalar(x); }
INLINE __device__ scalar cvt_to_scalar(uint64_t x) { return scalar(x); }
INLINE __device__ scalar cvt_to_scalar( int64_t x) { return scalar(x); }

// Additional conversion functions used for hardware texturing
#ifdef TARGET_CUDA
INLINE __device__ uint16_t half_to_ushort(half_t x) 
{
	return x.value;
}
INLINE __device__ ushort1 half_to_ushort(half_t1 a) 
{
	return make_ushort1(a.x.value);
}
INLINE __device__ ushort2 half_to_ushort(half_t2 a) 
{
	return make_ushort2(a.x.value,a.y.value);
}
INLINE __device__ ushort3 half_to_ushort(half_t3 a) 
{
	return make_ushort3(a.x.value,a.y.value,a.z.value);
}
INLINE __device__ ushort4 half_to_ushort(half_t4 a) 
{
	return make_ushort4(a.x.value,a.y.value,a.z.value,a.w.value);
}
#endif


#undef DECL_CVT
#undef DECL_CVTN

//=============================================================================
//		MATRIX ACCESSORS
//=============================================================================
namespace quasar
{

// Debugging - checking of the unsafe boundaries!

// Boundary checking code ('checked modifiers)
#define CHECK_BOUNDS(x, cond, defval)                                          \
    if (!(cond) && _error.target == NULL)                                      \
    {                                                                          \
        _error.target = (void *)(x).data;                                      \
        _error.target_bytes = sizeof(void *);                                  \
        _error.code = ERRORCODE_OUTOFBOUNDS;                                   \
        assert(0);                                                             \
        return defval;                                                         \
    }
#define V_CHECK_BOUNDS(x, cond, defval)                                        \
    if (!(cond) && _error.target == NULL)                                      \
    {                                                                          \
        _error.target = (void *)&x;                                            \
        _error.target_bytes = sizeof(void *);                                  \
        _error.code = ERRORCODE_OUTOFBOUNDS;                                   \
        assert(0);                                                             \
        return defval;                                                         \
    }

#if defined(CHECK_UNSAFE_BOUNDS)
#define CHECK_BOUNDS_FAST(x, cond, defval)									   \
	if (!(cond)) return defval;
#else
#define CHECK_BOUNDS_FAST(x, cond, defval)
#endif

#if defined(CHECK_NAN_OR_INF)
#undef CHECK_NAN_OR_INF
#define CHECK_NAN_OR_INF(tgt, x)                                               \
    if (_error.target == NULL && !_any_(_isfinite_(x)))                        \
    {                                                                          \
        _error.target = (tgt).data;                                            \
        _error.target_bytes = sizeof(void *);                                  \
        _error.code = ERRORCODE_NAN_OR_INF;                                    \
        assert(0);                                                             \
    }
#elif defined(CHECK_NAN)
#define CHECK_NAN_OR_INF(tgt, x)                                               \
    if (_error.target == NULL && _any_(_isnan_(x)))                            \
    {                                                                          \
        _error.target = (tgt).data;                                            \
        _error.target_bytes = sizeof(void *);                                  \
        _error.code = ERRORCODE_NAN;                                           \
        assert(0);                                                             \
    }
#else
#define CHECK_NAN_OR_INF(tgt, x)
#endif

// Accessors for vecX, ivecX, cvecX
#define VEC_AT(x, y) vecX_get_at(x, y)
#define IVEC_AT(x, y) ivecX_get_at(x, y)
#define CVEC_AT(x, y) cvecX_get_at(x, y)

    INLINE __device__ scalar &vecX_get_at(scalar1 &a, int pos)
    {
        return a.x;
    }
    INLINE __device__ scalar &vecX_get_at(scalar2 &a, int pos)
    {
        return ((scalar *)&(a))[pos];
    }
    INLINE __device__ scalar &vecX_get_at(scalar3 &a, int pos)
    {
        return ((scalar *)&(a))[pos];
    }
    INLINE __device__ scalar &vecX_get_at(scalar4 &a, int pos)
    {
        return ((scalar *)&(a))[pos];
    }
    template <int N>
    INLINE __device__ scalar &vecX_get_at(scalarN<N> &a, int pos)
    {
        return a.el[pos];
    }

    INLINE __device__ int &ivecX_get_at(int1 &a, int pos)
    {
        return a.x;
    }
    INLINE __device__ int &ivecX_get_at(int2 &a, int pos)
    {
        return ((int *)&(a))[pos];
    }
    INLINE __device__ int &ivecX_get_at(int3 &a, int pos)
    {
        return ((int *)&(a))[pos];
    }
    INLINE __device__ int &ivecX_get_at(int4 &a, int pos)
    {
        return ((int *)&(a))[pos];
    }
    template <int N> INLINE __device__ int &ivecX_get_at(intN<N> &a, int pos)
    {
        return a.el[pos];
    }

    INLINE __device__ cscalar &cvecX_get_at(cscalar1 &a, int pos)
    {
        return a.x;
    }
    INLINE __device__ cscalar &cvecX_get_at(cscalar2 &a, int pos)
    {
        return ((cscalar *)&(a))[pos];
    }
    INLINE __device__ cscalar &cvecX_get_at(cscalar3 &a, int pos)
    {
        return ((cscalar *)&(a))[pos];
    }
    INLINE __device__ cscalar &cvecX_get_at(cscalar4 &a, int pos)
    {
        return ((cscalar *)&(a))[pos];
    }
    template <int N>
    INLINE __device__ cscalar &cvecX_get_at(cscalarN<N> &a, int pos)
    {
        return a.el[pos];
    }

	// Support for the CUDA __lgd intrinsic
	//
	#if defined(TARGET_CUDA) && __CUDA_ARCH__ >= 350
	#define LOAD_CONST(x)		__ldg(x)
	#else
	#define LOAD_CONST(x)		(*(x))
	#endif

    //============================================================================
    // ACCESSOR TABLE
    //============+===============================================================
    // unchecked	|	vector_get_at			matrix_get_at
    // cube_get_at
    // checked		|	vector_get_at_checked	matrix_get_at_checked
    // cube_get_at_checked
    // safe			|	vector_get_at_safe
    // matrix_get_at_safe
    // cube_get_at_safe
    // circular		|	vector_get_at_circ
    // matrix_get_at_circ
    // cube_get_at_circ
    // mirror		|	vector_get_at_mir matrix_get_at_mir
    // cube_get_at_mir
    //
    // vector access:
    // unchecked	|	vector_get_vec_at
    // matrix_get_vec_at
    // cube_get_vec_at
    // checked		|	vector_get_vec_at_checked
    // matrix_get_vec_at_checked
    // cube_get_vec_at_checked
    // safe			|	vector_get_vec_at_safe
    // matrix_get_vec_at_safe
    // cube_get_vec_at_safe
    // circular		|
    // cube_get_vec_at_circular
    // mirror		|
    // cube_get_vec_at_mirror
    //
    // reference access:
    // unchecked	|	vector_get_ref_at
    // matrix_get_ref_at
    // cube_get_ref_at
    // checked		|	vector_get_ref_at_checked
    // matrix_get_ref_at_checked
    // cube_get_ref_at_checked
    // safe			|	vector_get_ref_at_safe
    // matrix_get_ref_at_safe
    // cube_get_ref_at_safe
    // circular		|	vector_get_ref_at_circ
    // matrix_get_ref_at_circ
    // cube_get_ref_at_circ
    // mirror		|	vector_get_ref_at_mir
    // matrix_get_ref_at_mir
    // cube_get_ref_at_mir
    //
    /// === FAST VERSIONS
    template <class T>
    INLINE __device__ T vector_get_at(const VectorBase<T> &x, int pos)
    {
		CHECK_BOUNDS_FAST(x, pos >= 0 && pos < x.dim1, T());
        return x.data[pos];
    }
    template <class T, class R>
    INLINE __device__ R vector_get_vec_at(const VectorBase<T> &x, int pos)
    {
		CHECK_BOUNDS_FAST(x, pos >= 0 && pos <= x.dim1 - ARRAY_LENGTH(R, T), R());
        return *((R *)&x.data[pos]);
    }
	template <class T>
    INLINE __device__ T vector_get_at(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_at(x, pos.x);
    }
    template <class T, class R>
    INLINE __device__ R vector_get_vec_at(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_vec_at(x, pos.x);
    }
    template <class T>
    INLINE __device__ T matrix_get_at(const MatrixBase<T> &x, int2 pos)
    {
		int index = pos.y + pos.x * x.dim2;
		CHECK_BOUNDS_FAST(x, index >= 0 && pos.x < x.dim1 * x.dim2, T());
        return x.data[index];
    }
    template <class T, class R>
    INLINE __device__ R matrix_get_vec_at(const MatrixBase<T> &x, int2 pos)
    {
		int index = pos.y + pos.x * x.dim2;
		CHECK_BOUNDS_FAST(x, index >= 0 && index <= x.dim1 * x.dim2 - ARRAY_LENGTH(R, T), R());
		return *(R *)&x.data[index];
    }
    // matrix_get_vec_at
    template <class T>
    INLINE __device__ T cube_get_at(const CubeBase<T> &x, int3 pos)
    {
		int index = pos.z + (pos.y + pos.x * x.dim2) * x.dim3;
		CHECK_BOUNDS_FAST(x, index >= 0 && index < x.dim1 * x.dim2 * x.dim3, T());
        return x.data[index];
    }
    template <class T, class R>
    INLINE __device__ R cube_get_vec_at(const CubeBase<T> &x, int3 pos)
    {
		int index = pos.z + (pos.y + pos.x * x.dim2) * x.dim3;
		CHECK_BOUNDS_FAST(x, index >= 0 && index <= x.dim1 * x.dim2 * x.dim3 - ARRAY_LENGTH(R, T), R());
		return *(R *)&x.data[index];
    }

    /// === CONSTANT MEM VERSIONS
    template <class T>
    INLINE __device__ T vector_get_at_const(const VectorBase<T> &x, int pos)
    {
        return LOAD_CONST(&((const T *const)x.data)[pos]);
    }
	template <class T>
    INLINE __device__ T vector_get_at_const(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_at_const(x, pos.x);
    }
    template <class T>
    INLINE __device__ T matrix_get_at_const(const MatrixBase<T> &x, int2 pos)
    {
        return LOAD_CONST(&((const T *const)x.data)[pos.y + pos.x * x.dim2]);
    }
    template <class T>
    INLINE __device__ T cube_get_at_const(const CubeBase<T> &x, int3 pos)
    {
        return LOAD_CONST(&((const T *const)x.data)[pos.z + (pos.y + pos.x * x.dim2) * x.dim3]);
    }
    template <class T, class R>
    INLINE __device__ R cube_get_vec_at_const(const CubeBase<T> &x, int3 pos)
    {
        return LOAD_CONST((R *)&((const T *const)
                           x.data)[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z]);
    }

    /// === CHECKED VERSIONS
    template <class T>
    INLINE __device__ T vector_get_at_checked(const VectorBase<T> &x, int pos)
    {
        CHECK_BOUNDS(x, pos >= 0 && pos < x.dim1, T());
        return x.data[pos];
    }
    template <class T>
    INLINE __device__ T vector_get_at_checked(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_at_checked(x, pos.x);
    }
    template <class T, class R>
    INLINE __device__ R
    vector_get_vec_at_checked(const VectorBase<T> &x, int pos)
    {
        CHECK_BOUNDS(x, pos >= 0 && pos <= x.dim1 - ARRAY_LENGTH(R, T), R());
        return *(R *)&x.data[pos];
    }
	template <class T, class R>
    INLINE __device__ R
    vector_get_vec_at_checked(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_vec_at_checked(x, pos.x);
    }
    template <class T>
    INLINE __device__ T matrix_get_at_checked(const MatrixBase<T> &x, int2 pos)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2,
                     T());
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T, class R>
    INLINE __device__ R
    matrix_get_vec_at_checked(const MatrixBase<T> &x, int2 pos)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y <= x.dim2 - ARRAY_LENGTH(R, T),
                     R());
        return *(R *)&x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T cube_get_at_checked(const CubeBase<T> &x, int3 pos)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2 && pos.z >= 0 && pos.z < x.dim3,
                     T());
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T, class R>
    INLINE __device__ R cube_get_vec_at_checked(const CubeBase<T> &x, int3 pos)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2 && pos.z >= 0 &&
                            pos.z <= x.dim3 - ARRAY_LENGTH(R, T),
                     R());
        return *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z];
    }

    /// === PRECISE VERSIONS, WITH BOUNDARY HANDLING
    template <class T>
    INLINE __device__ T vector_get_at_safe(const VectorBase<T> &x, int pos)
    {
        if (pos < 0 || pos >= x.dim1)
            return T();
        else
            return x.data[pos];
    }
    template <class T, class R>
    INLINE __device__ R vector_get_vec_at_safe(const VectorBase<T> &x, int pos)
    {
        if (pos < 0 || pos > x.dim1 - ARRAY_LENGTH(R, T))
            return R();
        else
            return *(R *)&x.data[pos];
    }
    template <class T>
    INLINE __device__ T vector_get_at_circ(const VectorBase<T> &x, int pos)
    {
        pos = periodize(pos, x.dim1);
        return x.data[pos];
    }
    template <class T>
    INLINE __device__ T vector_get_at_mir(const VectorBase<T> &x, int pos)
    {
        pos = mirror_ext(pos, x.dim1);
        return x.data[pos];
    }
    template <class T>
    INLINE __device__ T vector_get_at_clamped(const VectorBase<T> &x, int pos)
    {
        pos = clamp(pos, x.dim1 - 1);
        return x.data[pos];
    }

	template <class T>
    INLINE __device__ T vector_get_at_safe(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_at_safe(x, pos.x);
    }
    template <class T, class R>
    INLINE __device__ R vector_get_vec_at_safe(const VectorBase<T> &x, int1 pos)
    {
		return vecotr_get_vec_at_safe(x, pos.x);
    }
    template <class T>
    INLINE __device__ T vector_get_at_circ(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_at_circ(x, pos.x);
    }
    template <class T>
    INLINE __device__ T vector_get_at_mir(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_at_mir(x, pos.x);
    }
    template <class T>
    INLINE __device__ T vector_get_at_clamped(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_at_clamped(x, pos.x);
    }

    template <class T>
    INLINE __device__ T matrix_get_at_safe(const MatrixBase<T> &x, int2 pos)
    {
        if (pos.x < 0 || pos.y < 0 || pos.x >= x.dim1 || pos.y >= x.dim2)
            return T();
        else
            return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T, class R>
    INLINE __device__ R matrix_get_vec_at_safe(const MatrixBase<T> &x, int2 pos)
    {
        if (pos.x < 0 || pos.y < 0 || pos.x >= x.dim1 ||
            pos.y > x.dim2 - ARRAY_LENGTH(R, T))
            return R();
        else
            return *(R *)&x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T matrix_get_at_circ(const MatrixBase<T> &x, int2 pos)
    {
        pos.x = periodize(pos.x, x.dim1);
        pos.y = periodize(pos.y, x.dim2);
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T matrix_get_at_mir(const MatrixBase<T> &x, int2 pos)
    {
        pos.x = mirror_ext(pos.x, x.dim1);
        pos.y = mirror_ext(pos.y, x.dim2);
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T matrix_get_at_clamped(const MatrixBase<T> &x, int2 pos)
    {
        pos.x = clamp(pos.x, x.dim1 - 1);
        pos.y = clamp(pos.y, x.dim2 - 1);
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T cube_get_at_safe(const CubeBase<T> &x, int3 pos)
    {
        if (pos.x < 0 || pos.y < 0 || pos.z < 0 || pos.x >= x.dim1 ||
            pos.y >= x.dim2 || pos.z >= x.dim3)
            return T();
        else
            return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T>
    INLINE __device__ T cube_get_at_circ(const CubeBase<T> &x, int3 pos)
    {
        pos.x = periodize(pos.x, x.dim1);
        pos.y = periodize(pos.y, x.dim2);
        pos.z = periodize(pos.z, x.dim3);
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T>
    INLINE __device__ T cube_get_at_mir(const CubeBase<T> &x, int3 pos)
    {
        pos.x = mirror_ext(pos.x, x.dim1);
        pos.y = mirror_ext(pos.y, x.dim2);
        pos.z = mirror_ext(pos.z, x.dim3);
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T>
    INLINE __device__ T cube_get_at_clamped(const CubeBase<T> &x, int3 pos)
    {
        pos.x = clamp(pos.x, x.dim1 - 1);
        pos.y = clamp(pos.y, x.dim2 - 1);
        pos.z = clamp(pos.z, x.dim3 - 1);
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T, class R>
    INLINE __device__ R cube_get_vec_at_safe(const CubeBase<T> &x, int3 pos)
    {
        if (pos.x < 0 || pos.y < 0 || pos.x >= x.dim1 || pos.y >= x.dim2 ||
            pos.z < 0 || pos.z > x.dim3 - ARRAY_LENGTH(R, T))
            return R();
        else
            return *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z];
    }
    template <class T, class R>
    INLINE __device__ R cube_get_vec_at_circ(const CubeBase<T> &x, int3 pos)
    {
        pos.x = periodize(pos.x, x.dim1);
        pos.y = periodize(pos.y, x.dim2);
        return *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z];
    }
    template <class T, class R>
    INLINE __device__ R cube_get_vec_at_mir(const CubeBase<T> &x, int3 pos)
    {
        pos.x = mirror_ext(pos.x, x.dim1);
        pos.y = mirror_ext(pos.y, x.dim2);
        return *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z];
    }
    template <class T, class R>
    INLINE __device__ R cube_get_vec_at_clamped(const CubeBase<T> &x, int3 pos)
    {
        pos.x = clamp(pos.x, x.dim1 - 1);
        pos.y = clamp(pos.y, x.dim2 - 1);
        return *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z];
    }

    /// === FAST WRITE FUNCTIONS
    template <class T>
    INLINE __device__ T vector_set_at(const VectorBase<T> &x, int pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        x.data[pos] = val;
        return val;
    }
    template <class T>
    INLINE __device__ T vector_set_at(const VectorBase<T> &x, int1 pos, T val)
    {
        return vector_set_at(x, pos.x, val);
    }
    template <class T, class R>
    INLINE __device__ R
    vector_set_vec_at(const VectorBase<T> &x, int pos, R val)
    {
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[pos] = val;
        return val;
    }
    template <class T>
    INLINE __device__ T matrix_set_at(const MatrixBase<T> &x, int2 pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        x.data[pos.y + pos.x * x.dim2] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R
    matrix_set_vec_at(const MatrixBase<T> &x, int2 pos, R val)
    {
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[pos.y + pos.x * x.dim2] = val;
        return val;
    }
    template <class T>
    INLINE __device__ T cube_set_at(const CubeBase<T> &x, int3 pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R cube_set_vec_at(const CubeBase<T> &x, int3 pos, R val)
    {
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z] = val;
        return val;
    }

    /// === CHECKED WRITE FUNCTIONS
    template <class T>
    INLINE __device__ T
    vector_set_at_checked(const VectorBase<T> &x, int pos, T val)
    {
        CHECK_BOUNDS(x, pos >= 0 && pos < x.dim1, T());
        CHECK_NAN_OR_INF(x, val);
        x.data[pos] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R
    vector_set_vec_at_checked(const VectorBase<T> &x, int pos, R val)
    {
        CHECK_BOUNDS(x, pos >= 0 && pos <= x.dim1 - ARRAY_LENGTH(R, T), R());
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[pos] = val;
        return val;
    }
	template <class T>
    INLINE __device__ T
    vector_set_at_checked(const VectorBase<T> &x, int1 pos, T val)
    {
        return vector_set_at_checked(x, pos.x, val);
    }
    template <class T, class R>
    INLINE __device__ R
    vector_set_vec_at_checked(const VectorBase<T> &x, int1 pos, R val)
    {
        return vector_set_vec_at_checked(x, pos.x, val);
    }
    template <class T>
    INLINE __device__ T
    matrix_set_at_checked(const MatrixBase<T> &x, int2 pos, T val)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2,
                     T());
        CHECK_NAN_OR_INF(x, val);
        x.data[pos.y + pos.x * x.dim2] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R
    matrix_set_vec_at_checked(const MatrixBase<T> &x, int2 pos, R val)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y <= x.dim2 - ARRAY_LENGTH(R, T),
                     R());
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[pos.y + pos.x * x.dim2] = val;
        return val;
    }
    template <class T>
    INLINE __device__ T
    cube_set_at_checked(const CubeBase<T> &x, int3 pos, T val)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2 && pos.z >= 0 && pos.z < x.dim3,
                     T());
        CHECK_NAN_OR_INF(x, val);
        x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R
    cube_set_vec_at_checked(const CubeBase<T> &x, int3 pos, R val)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2 && pos.z >= 0 &&
                            pos.z <= x.dim3 - ARRAY_LENGTH(R, T),
                     R());
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3 + pos.z] = val;
        return val;
    }

    /// === SAFE WRITE FUNCTIONS
    template <class T>
    INLINE __device__ T
    vector_set_at_safe(const VectorBase<T> &x, int pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        if (pos >= 0 && pos < x.dim1)
            (T &)x.data[pos] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R
    vector_set_vec_at_safe(const VectorBase<T> &x, int pos, R val)
    {
        CHECK_NAN_OR_INF(x, val);
        if (pos >= 0 && pos <= x.dim1 - ARRAY_LENGTH(R, T))
            *(R *)&x.data[pos] = val;
        return val;
    }
	template <class T>
    INLINE __device__ T
    vector_set_at_safe(const VectorBase<T> &x, int1 pos, T val)
    {
        return vector_set_at_safe(x, pos.x, val);
    }
    template <class T, class R>
    INLINE __device__ R
    vector_set_vec_at_safe(const VectorBase<T> &x, int1 pos, R val)
    {
        return vector_set_vec_at_safe(x, pos.x, val);
    }
    template <class T>
    INLINE __device__ T
    matrix_set_at_safe(const MatrixBase<T> &x, int2 pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        if (pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 && pos.y < x.dim2)
            x.data[pos.y + pos.x * x.dim2] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R
    matrix_set_vec_at_safe(const MatrixBase<T> &x, int2 pos, R val)
    {
        CHECK_NAN_OR_INF(x, val);
        if (pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
            pos.y <= x.dim2 - ARRAY_LENGTH(R, T))
            *(R *)&x.data[pos.y + pos.x * x.dim2] = val;
        return val;
    }
    template <class T>
    INLINE __device__ T cube_set_at_safe(const CubeBase<T> &x, int3 pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        if (pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 && pos.y < x.dim2 &&
            pos.z >= 0 && pos.z < x.dim3)
            x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3] = val;
        return val;
    }
    template <class T, class R>
    INLINE __device__ R
    cube_set_vec_at_safe(const CubeBase<T> &x, int3 pos, R val)
    {
        CHECK_NAN_OR_INF(x, val);
        if (pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 && pos.y < x.dim2 &&
            pos.z >= 0 && pos.z <= x.dim3 - ARRAY_LENGTH(R, T))
            *(R *)&x.data[(pos.y + pos.x * x.dim2) * x.dim3] = val;
        return val;
    }

    //=== REFERENCE ACCESS:
    template <class T>
    INLINE __device__ T &vector_get_ref_at(const VectorBase<T> &x, int pos)
    {
        return x.data[pos];
    }
	template <class T>
    INLINE __device__ T &vector_get_ref_at(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_ref_at(x, pos.x);
    }
    template <class T>
    INLINE __device__ T &matrix_get_ref_at(const MatrixBase<T> &x, int2 pos)
    {
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T &cube_get_ref_at(const CubeBase<T> &x, int3 pos)
    {
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }

    /// === CHECKED VERSIONS
    template <class T>
    INLINE __device__ T &vector_get_ref_at_checked(const VectorBase<T> &x,
                                                   int pos)
    {
        CHECK_BOUNDS(x, pos >= 0 && pos < x.dim1, C_NULL_REF(T));
        return x.data[pos];
    }
	template <class T>
    INLINE __device__ T &vector_get_ref_at_checked(const VectorBase<T> &x,
                                                   int1 pos)
    {
        return vector_get_ref_at_checked(x, pos.x);
    }
    template <class T>
    INLINE __device__ T &matrix_get_ref_at_checked(const MatrixBase<T> &x,
                                                   int2 pos)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2,
                     C_NULL_REF(T));
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T &cube_get_ref_at_checked(const CubeBase<T> &x, int3 pos)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2 && pos.z >= 0 && pos.z < x.dim3,
                     C_NULL_REF(T));
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    /// === PRECISE VERSIONS, WITH BOUNDARY HANDLING
    template <class T>
    INLINE __device__ T &vector_get_ref_at_safe(const VectorBase<T> &x, int pos)
    {
        if (pos < 0 || pos >= x.dim1)
            return C_NULL_REF(T);
        else
            return x.data[pos];
    }
    template <class T>
    INLINE __device__ T &vector_get_ref_at_circ(const VectorBase<T> &x, int pos)
    {
        return x.data[periodize(pos, x.dim1)];
    }
    template <class T>
    INLINE __device__ T &vector_get_ref_at_mir(const VectorBase<T> &x, int pos)
    {
        return x.data[mirror_ext(pos, x.dim1)];
    }
    template <class T>
    INLINE __device__ T &vector_get_ref_at_clamped(const VectorBase<T> &x,
                                                   int pos)
    {
        return x.data[clamp(pos, x.dim1 - 1)];
    }

	template <class T>
    INLINE __device__ T &vector_get_ref_at_safe(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_ref_at_safe(x, pos.x);
    }
    template <class T>
    INLINE __device__ T &vector_get_ref_at_circ(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_ref_at_circ(x, pos.x);
    }
    template <class T>
    INLINE __device__ T &vector_get_ref_at_mir(const VectorBase<T> &x, int1 pos)
    {
        return vector_get_ref_at_mir(x, pos.x);
    }
    template <class T>
    INLINE __device__ T &vector_get_ref_at_clamped(const VectorBase<T> &x,
                                                   int1 pos)
    {
        return vector_get_ref_at_clamped(x, pos.x);
    }

    template <class T>
    INLINE __device__ T &matrix_get_ref_at_safe(const MatrixBase<T> &x,
                                                int2 pos)
    {
        if (pos.x < 0 || pos.y < 0 || pos.x >= x.dim1 || pos.y >= x.dim2)
            return C_NULL_REF(T);
        else
            return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T &matrix_get_ref_at_circ(const MatrixBase<T> &x,
                                                int2 pos)
    {
        pos.x = periodize(pos.x, x.dim1);
        pos.y = periodize(pos.y, x.dim2);
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T &matrix_get_ref_at_mir(const MatrixBase<T> &x, int2 pos)
    {
        pos.x = mirror_ext(pos.x, x.dim1);
        pos.y = mirror_ext(pos.y, x.dim2);
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T &matrix_get_ref_at_clamped(const MatrixBase<T> &x,
                                                   int2 pos)
    {
        pos.x = clamp(pos.x, x.dim1 - 1);
        pos.y = clamp(pos.y, x.dim2 - 1);
        return x.data[pos.y + pos.x * x.dim2];
    }
    template <class T>
    INLINE __device__ T &cube_get_ref_at_safe(const CubeBase<T> &x, int3 pos)
    {
        if (pos.x < 0 || pos.y < 0 || pos.z < 0 || pos.x >= x.dim1 ||
            pos.y >= x.dim2 || pos.z >= x.dim3)
            return C_NULL_REF(T);
        else
            return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T>
    INLINE __device__ T &cube_get_ref_at_circ(const CubeBase<T> &x, int3 pos)
    {
        pos.x = periodize(pos.x, x.dim1);
        pos.y = periodize(pos.y, x.dim2);
        pos.z = periodize(pos.z, x.dim3);
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T>
    INLINE __device__ T &cube_get_ref_at_mir(const CubeBase<T> &x, int3 pos)
    {
        pos.x = mirror_ext(pos.x, x.dim1);
        pos.y = mirror_ext(pos.y, x.dim2);
        pos.z = mirror_ext(pos.z, x.dim3);
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
    template <class T>
    INLINE __device__ T &cube_get_ref_at_clamped(const CubeBase<T> &x, int3 pos)
    {
        pos.x = clamp(pos.x, x.dim1 - 1);
        pos.y = clamp(pos.y, x.dim2 - 1);
        pos.z = clamp(pos.z, x.dim3 - 1);
        return x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3];
    }
} // namespace quasar

//=============================================================================
// ATOMIC OPERATIONS
//=============================================================================


// TODO - check MAC versions
// OSAtomicCompareAndSwapPtr should be defined in "#include
// <libkern/OSAtomic.h>"
//#if __ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED__ >= 1050
//#define atomicCAS
// OSAtomicCompareAndSwapPtr
//(old_value, new_value, ptr);

// CUDA
#if defined(TARGET_CUDA)

// Define some signed overloads for the existing atomicCAS functions
INLINE __device__ long atomicCAS(long *x, long oldval, long newval)
{
    return atomicCAS((unsigned int *)(x), (unsigned int)oldval,
                     (unsigned int)newval);
}
INLINE __device__ long long atomicCAS(long long *x, long long oldval,
                                      long long newval)
{
    return atomicCAS((unsigned long long *)(x), (unsigned long long)oldval,
                     (unsigned long long)newval);
}
INLINE __device__ uint16_t atomicCAS(uint16_t *x, uint16_t oldval, uint16_t newval)
{
	int odd = (size_t)x & 1;
    long *base_address = (long *)((size_t)x & ~2);
	long init = *base_address;
	long old_val = odd ? (init & 0xffffu) | (oldval << 16) : (init & 0xffff0000u) | oldval;
	long new_val = odd ? (init & 0xffffu) | (newval << 16) : (init & 0xffff0000u) | newval;
	long result = atomicCAS(base_address, old_val, new_val);
    return odd ? (uint32_t)result >> 16 : ((uint32_t)result & 0xffffu);
}

#define INT_ATOMIC_ADD_DEFINED
#define INT_ATOMIC_SUB_DEFINED
#define FLOAT_ATOMIC_ADD_DEFINED
#define FLOAT_ATOMIC_SUB_DEFINED

// 2. GCC
#elif defined(__GNUG__)

INLINE __device__ uint16_t atomicCAS(uint16_t *x, uint16_t oldval,
                                         uint16_t newval)
{
    return __sync_val_compare_and_swap(x, oldval, newval);
}

INLINE __device__ unsigned int atomicCAS(unsigned int *x, unsigned int oldval,
                                         unsigned int newval)
{
    return __sync_val_compare_and_swap(x, oldval, newval);
}
INLINE __device__ unsigned long long atomicCAS(unsigned long long *x,
                                               unsigned long long oldval,
                                               unsigned long long newval)
{
    return __sync_val_compare_and_swap(x, oldval, newval);
}
INLINE __device__ long atomicCAS(long *x, long oldval, long newval)
{
    return __sync_val_compare_and_swap(x, oldval, newval);
}
INLINE __device__ long long atomicCAS(long long *x, long long oldval,
                                      long long newval)
{
    return __sync_val_compare_and_swap(x, oldval, newval);
}
namespace quasar
{

    INLINE __device__ int _atomicAdd(int *a, int b)
    {
        return __sync_add_and_fetch(a, b);
    }
    INLINE __device__ int _atomicSub(int *a, int b)
    {
        return __sync_sub_and_fetch(a, b);
    }

} // namespace quasar

#define INT_ATOMIC_ADD_DEFINED
#define INT_ATOMIC_SUB_DEFINED

// 2. Visual C++
#elif defined(_MSC_VER)

// Some 'evil' header files for Windows (just necessary for the
// __popcnt and InterlockedCompareExchange functions)
#include <intrin.h>

#pragma intrinsic(_InterlockedCompareExchange, _InterlockedCompareExchange64)

INLINE __device__ uint16_t atomicCAS(uint16_t *x, uint16_t oldval,
                                     uint16_t newval)
{
    return _InterlockedCompareExchange16((short *)(x), (short)(newval),
                                         (short)(oldval));
}
INLINE __device__ unsigned int atomicCAS(unsigned int *x, unsigned int oldval,
                                         unsigned int newval)
{
    return _InterlockedCompareExchange((long *)(x), (long)(newval),
                                       (long)(oldval));
}
INLINE __device__ unsigned long long atomicCAS(unsigned long long *x,
                                               unsigned long long oldval,
                                               unsigned long long newval)
{
    return _InterlockedCompareExchange64((long long *)(x), (long long)(newval),
                                         (long long)(oldval));
}
INLINE __device__ int atomicCAS(long *x, long oldval, long newval)
{
    return _InterlockedCompareExchange((long *)x, newval, oldval);
}
INLINE __device__ long long atomicCAS(long long *x, long long oldval,
                                      long long newval)
{
    return _InterlockedCompareExchange64(x, newval, oldval);
}

#else
#error "Atomic operations not implemented yet for this target"
#endif

namespace quasar
{

// Integers - operator
#define DECL_INT_ATOMIC_OP(name, X)                                            \
    INLINE __device__ int _atomic##name(int *pd, int d)                        \
    {                                                                          \
        union                                                                  \
        {                                                                      \
            unsigned int iOld;                                                 \
            int dOld;                                                          \
        };                                                                     \
        union                                                                  \
        {                                                                      \
            unsigned int iNew;                                                 \
            int dNew;                                                          \
        };                                                                     \
        for (;;)                                                               \
        {                                                                      \
            iOld = *(unsigned int *)pd;                                        \
            dNew = dOld X d;                                                   \
            if (atomicCAS((unsigned int *)pd, iOld, iNew) == iOld)             \
                return dNew;                                                   \
        }                                                                      \
    }
// Integers - function
#define DECL_INT_ATOMIC_OP_FN(name, fn)                                        \
    INLINE __device__ int _atomic##name(int *pd, int d)                        \
    {                                                                          \
        union                                                                  \
        {                                                                      \
            unsigned int iOld;                                                 \
            int dOld;                                                          \
        };                                                                     \
        union                                                                  \
        {                                                                      \
            unsigned int iNew;                                                 \
            int dNew;                                                          \
        };                                                                     \
        for (;;)                                                               \
        {                                                                      \
            iOld = *(unsigned int *)pd;                                        \
            dNew = fn(dOld, d);                                                \
            if (atomicCAS((unsigned int *)pd, iOld, iNew) == iOld)             \
                return dNew;                                                   \
        }                                                                      \
    }
// Float32 - operator
#define DECL_FLOAT_ATOMIC_OP(name, X)                                          \
    INLINE __device__ float _atomic##name(float *pd, float d)                  \
    {                                                                          \
        union                                                                  \
        {                                                                      \
            unsigned int iOld;                                                 \
            float dOld;                                                        \
        };                                                                     \
        union                                                                  \
        {                                                                      \
            unsigned int iNew;                                                 \
            float dNew;                                                        \
        };                                                                     \
        for (;;)                                                               \
        {                                                                      \
            iOld = *(unsigned int *)pd;                                        \
            dNew = dOld X d;                                                   \
            if (atomicCAS((unsigned int *)pd, iOld, iNew) == iOld)             \
                return dNew;                                                   \
        }                                                                      \
    }
// Float32 - function
#define DECL_FLOAT_ATOMIC_OP_FN(name, fn)                                      \
    INLINE __device__ float _atomic##name(float *pd, float d)                  \
    {                                                                          \
        union                                                                  \
        {                                                                      \
            unsigned int iOld;                                                 \
            float dOld;                                                        \
        };                                                                     \
        union                                                                  \
        {                                                                      \
            unsigned int iNew;                                                 \
            float dNew;                                                        \
        };                                                                     \
        for (;;)                                                               \
        {                                                                      \
            iOld = *(unsigned int *)pd;                                        \
            dNew = fn(dOld, d);                                                \
            if (atomicCAS((unsigned int *)pd, iOld, iNew) == iOld)             \
                return dNew;                                                   \
        }                                                                      \
    }
// Float64 - operator
#define DECL_DOUBLE_ATOMIC_OP(name, X)                                         \
    INLINE __device__ double _atomic##name(double *pd, double d)               \
    {                                                                          \
        union                                                                  \
        {                                                                      \
            long long iOld;                                                    \
            double dOld;                                                       \
        };                                                                     \
        union                                                                  \
        {                                                                      \
            long long iNew;                                                    \
            double dNew;                                                       \
        };                                                                     \
        for (;;)                                                               \
        {                                                                      \
            iOld = *(long long *)pd;                                           \
            dNew = dOld X d;                                                   \
            if (atomicCAS((long long *)pd, iOld, iNew) == iOld)                \
                return dNew;                                                   \
        }                                                                      \
    }
// Float64 - function
#define DECL_DOUBLE_ATOMIC_OP_FN(name, fn)                                     \
    INLINE __device__ double _atomic##name(double *pd, double d)               \
    {                                                                          \
        union                                                                  \
        {                                                                      \
            long long iOld;                                                    \
            double dOld;                                                       \
        };                                                                     \
        union                                                                  \
        {                                                                      \
            long long iNew;                                                    \
            double dNew;                                                       \
        };                                                                     \
        for (;;)                                                               \
        {                                                                      \
            iOld = *(long long *)pd;                                           \
            dNew = fn(dOld, d);                                                \
            if (atomicCAS((long long *)pd, iOld, iNew) == iOld)                \
                return dNew;                                                   \
        }                                                                      \
    }

// Float16 - operator
#define DECL_HALF_ATOMIC_OP(name, X)                                           \
    INLINE __device__ half_t _atomic##name(half_t *pd, half_t d)               \
    {                                                                          \
		half_t hOld, hNew;													   \
        for (;;)                                                               \
        {                                                                      \
            hOld = *pd;														   \
            hNew = (half_t)(float(hOld) X float(d));                           \
            if (atomicCAS((uint16_t *)pd, hOld.value, hNew.value) == hOld.value)  \
                return hNew;                                                   \
        }                                                                      \
    }

// Float16 - function
#define DECL_HALF_ATOMIC_OP_FN(name, fn)                                       \
    INLINE __device__ half_t _atomic##name(half_t *pd, half_t d)               \
    {                                                                          \
		half_t hOld, hNew;													   \
        for (;;)                                                               \
        {                                                                      \
            hOld = *pd;														   \
            hNew = (half_t)fn((float)hOld, (float)d);                          \
            if (atomicCAS((uint16_t *)pd, hOld.value, hNew.value) == hOld.value)  \
                return hNew;                                                   \
        }                                                                      \
    }


#ifndef INT_ATOMIC_ADD_DEFINED
    DECL_INT_ATOMIC_OP(Add, +)
#endif
#ifndef INT_ATOMIC_SUB_DEFINED
    DECL_INT_ATOMIC_OP(Sub, -)
#endif
    DECL_INT_ATOMIC_OP(Mul, *)
    DECL_INT_ATOMIC_OP(Div, / )
    DECL_INT_ATOMIC_OP_FN(Pow, pow)
    DECL_INT_ATOMIC_OP_FN(Min, min)
    DECL_INT_ATOMIC_OP_FN(Max, max)
    DECL_INT_ATOMIC_OP(Xor, ^)
    DECL_INT_ATOMIC_OP(And, &)
    DECL_INT_ATOMIC_OP(Or, | )

#ifndef FLOAT_ATOMIC_ADD_DEFINED
    DECL_FLOAT_ATOMIC_OP(Add, +)
#endif
#ifndef FLOAT_ATOMIC_SUB_DEFINED
    DECL_FLOAT_ATOMIC_OP(Sub, -)
#endif
    DECL_FLOAT_ATOMIC_OP(Mul, *)
    DECL_FLOAT_ATOMIC_OP(Div, / )
    DECL_FLOAT_ATOMIC_OP_FN(Pow, pow)
    DECL_FLOAT_ATOMIC_OP_FN(Min, min)
    DECL_FLOAT_ATOMIC_OP_FN(Max, max)

#ifndef DOUBLE_ATOMIC_ADD_DEFINED
    DECL_DOUBLE_ATOMIC_OP(Add, +)
#endif
#ifndef DOUBLE_ATOMIC_SUB_DEFINED
    DECL_DOUBLE_ATOMIC_OP(Sub, -)
#endif
    DECL_DOUBLE_ATOMIC_OP(Mul, *)
    DECL_DOUBLE_ATOMIC_OP(Div, / )
    DECL_DOUBLE_ATOMIC_OP_FN(Pow, pow)
    DECL_DOUBLE_ATOMIC_OP_FN(Min, min)
    DECL_DOUBLE_ATOMIC_OP_FN(Max, max)

    DECL_HALF_ATOMIC_OP(Add, +)
    DECL_HALF_ATOMIC_OP(Sub, -)
    DECL_HALF_ATOMIC_OP(Mul, *)
    DECL_HALF_ATOMIC_OP(Div, / )
    DECL_HALF_ATOMIC_OP_FN(Pow, pow)
    DECL_HALF_ATOMIC_OP_FN(Min, min)
    DECL_HALF_ATOMIC_OP_FN(Max, max)


// CUDA - special instructions are available for atomicAdd
#ifdef TARGET_CUDA
#if !defined(DBL_SCALAR)
    INLINE __device__ scalar _atomicAdd(scalar *a, scalar b)
    {
        return atomicAdd((scalar *)a, b) + b;
    }
    INLINE __device__ scalar _atomicSub(scalar *a, scalar b)
    {
        return atomicAdd((scalar *)a, -b) - b;
    }
#endif

    INLINE __device__ int _atomicAdd(int *a, int b)
    {
        return atomicAdd((int *)a, b) + b;
    }
    INLINE __device__ int _atomicSub(int *a, int b)
    {
        return atomicAdd((int *)a, -b) - b;
    }
#endif

    //
    // Atomic operations for cscalar numbers
    //
    // TO DO - operation is not atomic yet... A possible solution might be to
    // use
    // the atomicCAS function, although we need 128-bit integers for the
    // DBL_SCALAR
    // case.
    //
	#ifdef HALF_SCALAR
	#define cscalar_atomicT cscalar_t<half_t>
	#else
	#define cscalar_atomicT cscalar
	#endif

    INLINE __device__ cscalar_atomicT _atomicAdd(cscalar_atomicT *a, cscalar_atomicT b)
    {
        return cscalar_atomicT(_atomicAdd(&a->x, b.x), _atomicAdd(&a->y, b.y));
    }
    INLINE __device__ cscalar_atomicT _atomicSub(cscalar_atomicT *a, cscalar_atomicT b)
    {
        return cscalar_atomicT(_atomicSub(&a->x, b.x), _atomicSub(&a->y, b.y));
    }
    INLINE __device__ cscalar_atomicT _atomicMul(cscalar_atomicT *a, cscalar_atomicT b)
    {
        return cscalar_atomicT(_atomicMul(&a->x, b.x), _atomicMul(&a->y, b.y));
    }
    INLINE __device__ cscalar_atomicT _atomicDiv(cscalar_atomicT *a, cscalar_atomicT b)
    {
        return cscalar_atomicT(_atomicDiv(&a->x, b.x), _atomicDiv(&a->y, b.y));
    }
    INLINE __device__ cscalar_atomicT _atomicPow(cscalar_atomicT *a, cscalar_atomicT b)
    {
        return cscalar_atomicT(_atomicPow(&a->x, b.x), _atomicPow(&a->y, b.y));
    }
	#undef cscalar_atomicT

	// Vector extensions (note: these are non truly atomic)
	#define BINARY_FUNCTION_T(X, T)                                           \
    INLINE T##1 __device__(X)(T##1* a, T##1 b)                                 \
    {                                                                          \
        return make_##T##1(X(&a->x, b.x));                                     \
    }                                                                          \
    INLINE T##2 __device__(X)(T##2* a, T##2 b)                                 \
    {                                                                          \
        return make_##T##2(X(&a->x, b.x), X(&a->y, b.y));                      \
    }                                                                          \
    INLINE T##3 __device__(X)(T##3* a, T##3 b)                                 \
    {                                                                          \
        return make_##T##3(X(&a->x, b.x), X(&a->y, b.y), X(&a->z, b.z));       \
    }                                                                          \
    INLINE T##4 __device__(X)(T##4* a, T##4 b)                                 \
    {                                                                          \
        return make_##T##4(X(&a->x, b.x), X(&a->y, b.y), X(&a->z, b.z),        \
                           X(&a->w, b.w));                                     \
    }                                                                          \
    template <int N> INLINE T##N<N> __device__(X)(T##N<N>* a, T##N<N> b)       \
    {                                                                          \
        T##N<N> y;                                                             \
        FOR_ALL(y.el[i] = X(&a->el[i], b.el[i]));                              \
        return y;                                                              \
    }

	BINARY_FUNCTION_T(_atomicAdd, int);
	BINARY_FUNCTION_T(_atomicSub, int);
	BINARY_FUNCTION_T(_atomicMul, int);
	BINARY_FUNCTION_T(_atomicDiv, int);
	BINARY_FUNCTION_T(_atomicPow, int);
	BINARY_FUNCTION_T(_atomicMin, int);
	BINARY_FUNCTION_T(_atomicMax, int);

	BINARY_FUNCTION_T(_atomicAdd, scalar);
	BINARY_FUNCTION_T(_atomicSub, scalar);
	BINARY_FUNCTION_T(_atomicMul, scalar);
	BINARY_FUNCTION_T(_atomicDiv, scalar);
	BINARY_FUNCTION_T(_atomicPow, scalar);
	BINARY_FUNCTION_T(_atomicMin, scalar);
	BINARY_FUNCTION_T(_atomicMax, scalar);

#ifndef HALF_SCALAR
	BINARY_FUNCTION_T(_atomicAdd, cscalar);
	BINARY_FUNCTION_T(_atomicSub, cscalar);
	BINARY_FUNCTION_T(_atomicMul, cscalar);
	BINARY_FUNCTION_T(_atomicDiv, cscalar);
	BINARY_FUNCTION_T(_atomicPow, cscalar);
#endif

    // 1. VectorBase<T>
    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T vector_at(const VectorBase<T> x, int pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        return atomic_function((T *)&x.data[pos], val);
    }

    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T vector_at_safe(const VectorBase<T> x, int pos, T val)
    {
        T retVal;
        CHECK_NAN_OR_INF(x, val);
        if (pos >= 0 && pos < x.dim1)
            retVal = atomic_function((T *)&x.data[pos], val);
        return retVal;
    }

    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T vector_at_checked(const VectorBase<T> x, int pos, T val)
    {
        CHECK_BOUNDS(x, pos >= 0 && pos < x.dim1, T());
        CHECK_NAN_OR_INF(x, val);
        return atomic_function((T *)&x.data[pos], val);
    }

    // 2. MatrixBase<T>
    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T matrix_at(const MatrixBase<T> x, int2 pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        return atomic_function((T *)&x.data[pos.y + pos.x * x.dim2], val);
    }

    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T matrix_at_safe(const MatrixBase<T> x, int2 pos, T val)
    {
        T retVal;
        CHECK_NAN_OR_INF(x, val);
        if (pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 && pos.y < x.dim2)
            retVal = atomic_function((T *)&x.data[pos.y + pos.x * x.dim2], val);
        return retVal;
    }

    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T
    matrix_at_checked(const MatrixBase<T> x, int2 pos, T val)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2,
                     T());
        CHECK_NAN_OR_INF(x, val);
        return atomic_function((T *)&x.data[pos.y + pos.x * x.dim2], val);
    }


    // 3. CubeBase<T>
    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T cube_at(const CubeBase<T> x, int3 pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        return atomic_function(
            (T *)&x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3], val);
    }

    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T cube_at_safe(const CubeBase<T> x, int3 pos, T val)
    {
        T retVal;
        CHECK_NAN_OR_INF(x, val);
        if (pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 && pos.y < x.dim2)
            retVal = atomic_function(
                (T *)&x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3], val);
        return retVal;
    }

    template <class T, T atomic_function(T *, T)>
    INLINE __device__ T cube_at_checked(const CubeBase<T> x, int3 pos, T val)
    {
        CHECK_BOUNDS(x, pos.x >= 0 && pos.x < x.dim1 && pos.y >= 0 &&
                            pos.y < x.dim2 && pos.z >= 0 && pos.z < x.dim3,
                     T());
        CHECK_NAN_OR_INF(x, val);
        return atomic_function(
            (T *)&x.data[pos.z + (pos.y + pos.x * x.dim2) * x.dim3], val);
    }

} // namespace quasar

//=============================================================================
// SUPPORT FOR CONSTANT MEMORY (currently, we allow max 1 KB)
//============+================================================================
#define MAX_CONST_MEM 8192
#ifdef TARGET_CUDA
__device__ __constant__ unsigned char const_mem[MAX_CONST_MEM];
#endif

//=============================================================================
// SUPPORT FOR SHARED MEMORY
//=============================================================================

#ifdef TARGET_CUDA
extern __shared__ scalar shared_mem[];
__device__ int shared_mem_dwords;
#elif defined(__QUASAR_NO_EXPORTS__)
extern "C" {
EXPORT extern scalar *shared_mem;
}
extern "C" {
EXPORT extern int shared_mem_dwords;
}
#else
extern "C" {
EXPORT SELECTANY scalar *shared_mem = NULL;
}
extern "C" {
EXPORT SELECTANY int shared_mem_dwords = 0;
}
#endif
typedef int shmem;

namespace quasar
{

    // Parallel fill algorithm (for memory initizialization)
    template <class T>
    INLINE void __device__ shmem_fill(T *ptr, int nelem, T val)
    {
        int nThreads = __threadcnt();
        int tid = __threadidx();
        for (int i = tid; i < nelem; i += nThreads)
            ptr[i] = val;
    }

    template <class T>
    INLINE void __device__ mem_fill(T *ptr, int nscalars, T val)
    {
        for (int i = 0; i < nscalars; i++)
            ptr[i] = val;
    }


    template <class T>
    INLINE __device__ VectorBase<T> make_vector(T *data, int dim1)
    {
        VectorBase<T> v;
        v.dim1 = dim1;
        v.data = data;
        return v;
    }

    template <class T>
    INLINE __device__ MatrixBase<T> make_matrix(T *data, int dim1, int dim2)
    {
        MatrixBase<T> m;
        m.dim1 = dim1;
        m.dim2 = dim2;
        m.data = data;
        return m;
    }

    template <class T>
    INLINE __device__ CubeBase<T> make_cube(T *data, int dim1, int dim2,
                                            int dim3)
    {
        CubeBase<T> c;
        c.dim1 = dim1;
        c.dim2 = dim2;
        c.dim3 = dim3;
        c.data = data;
        return c;
    }


    INLINE void __device__ shmem_init(shmem *_shmem) { *_shmem = 0; }

    // allocate shared memory
    template <class T>
    INLINE __device__ T *shmem_allocate(shmem *_shmem, int nelem)
    {
        T *retval;
        retval = (T *)&shared_mem[*_shmem];
        shared_mem_dwords = (*_shmem += nelem * (sizeof(T) / sizeof(scalar)));
        if (*_shmem > 32768) // shared memory overrun
        {
            _error.target = shared_mem;
            _error.target_bytes = *_shmem;
            _error.code = ERRORCODE_DYNMEM_OUTOFMEMORY;
            assert(0);
        }
        return retval;
    }

    template <class T>
    INLINE __device__ VectorBase<T> shmem_alloc(shmem *_shmem, int dim1)
    {
        return make_vector(shmem_allocate<T>(_shmem, dim1), dim1);
    }

    template <class T>
    INLINE __device__ MatrixBase<T> shmem_alloc(shmem *_shmem, int dim1,
                                                int dim2)
    {
        return make_matrix(shmem_allocate<T>(_shmem, dim1 * dim2), dim1, dim2);
    }

    template <class T>
    INLINE __device__ CubeBase<T> shmem_alloc(shmem *_shmem, int dim1, int dim2,
                                              int dim3)
    {
        return make_cube(shmem_allocate<T>(_shmem, dim1 * dim2 * dim3), dim1,
                         dim2, dim3);
    }


    template <class T>
    INLINE __device__ VectorBase<T> shmem_zeros(shmem *_shmem, int dim1)
    {
        VectorBase<T> v = make_vector(shmem_allocate<T>(_shmem, dim1), dim1);
        shmem_fill<T>(v.data, v.dim1, (T)0.0f);
        return v;
    }

    template <class T>
    INLINE __device__ MatrixBase<T> shmem_zeros(shmem *_shmem, int dim1,
                                                int dim2)
    {
        MatrixBase<T> m =
            make_matrix(shmem_allocate<T>(_shmem, dim1 * dim2), dim1, dim2);
        shmem_fill<T>(m.data, m.dim1 * m.dim2, (T)0.0f);
        return m;
    }

    template <class T>
    INLINE __device__ CubeBase<T> shmem_zeros(shmem *_shmem, int dim1, int dim2,
                                              int dim3)
    {
        CubeBase<T> c = make_cube(shmem_allocate<T>(_shmem, dim1 * dim2 * dim3),
                                  dim1, dim2, dim3);
        shmem_fill<T>(c.data, c.dim1 * c.dim2 * c.dim3, 0.0f);
        return c;
    }

    //=============================================================================
    // USER ASSERTIONS
    //=============================================================================
    INLINE __device__ void user_assert(int x, const char *msg, int len)
    {
        if (!(x))
        {
            _error.target = msg;
            _error.target_bytes = len;
            _error.code = ERRORCODE_ASSERTIONFAILED;
            assert(0);
        }
    }

    INLINE __device__ void user_assert(int1 a, const char *msg, int len)
    {
        user_assert(a.x, msg, len);
    }
    INLINE __device__ void user_assert(int2 a, const char *msg, int len)
    {
        user_assert(a.x & a.y, msg, len);
    }
    INLINE __device__ void user_assert(int3 a, const char *msg, int len)
    {
        user_assert(a.x & a.y & a.z, msg, len);
    }
    INLINE __device__ void user_assert(int4 a, const char *msg, int len)
    {
        user_assert(a.x & a.y & a.z & a.w, msg, len);
    }
    template <int N>
    __device__ void user_assert(intN<N> a, const char *msg, int len)
    {
        int x = a.el[0];
        for (int i = 1; i < N; i++)
            x &= a.el[i];
        user_assert(x, msg, len);
    }

} // namespace quasar


//=============================================================================
// Parallel memory allocation (to be used from __kernel__ and __device__
// functions)
//=============================================================================

#ifdef TARGET_CUDA

#elif defined(__GNUG__)

#define __ffs(x) __builtin_ffs(x)
#define __popc(x) __builtin_popcount(x)

#elif defined(_MSC_VER)

// Microsoft C/C++ has different built-in functions for ffs
// Note: long is 32-bit with Visual C++ (both x86 and x64)
static __device__ int __ffs(unsigned int x)
{
    unsigned int index;
    return _BitScanForward((unsigned long *)&index, x) ? index + 1 : 0;
}
static __device__ int __popc(unsigned int v)
{
    v = v - ((v >> 1) & 0x55555555);
    v = (v & 0x33333333) + ((v >> 2) & 0x33333333);
    return ((v + (v >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;
}
#endif

#ifndef TARGET_CUDA

// Atomic AND
INLINE __device__ unsigned int atomicAnd(unsigned int *x, unsigned int mask)
{
#pragma omp atomic
    *x &= mask;
    return *x;
}

#endif

#ifdef ENABLE_DYNAMIC_KERNEL_MEM
#include "quasar_dynmem.h"
#elif !defined(DYNMEM2_0)

namespace quasar
{

    // Smart object pointer - now much simpler
    template <class T> class object_ptr
    {
      protected:
        T *_ptr;

      public:
        INLINE __device__ object_ptr() : _ptr(NULL) {}
        INLINE __device__ object_ptr(T *ptr) : _ptr(ptr) {}
        INLINE __device__ operator T *&() { return (T *&)_ptr; }
        INLINE __device__ T *operator->() { return (T *)_ptr; }
        INLINE __device__ void release() {}
        INLINE __device__ void addref() {}
    };

    template <class T> INLINE __device__ void VectorBase<T>::addref() {}
    template <class T> INLINE __device__ void MatrixBase<T>::addref() {}
    template <class T> INLINE __device__ void CubeBase<T>::addref() {}

    template <class T> INLINE __device__ void VectorBase<T>::release() {}
    template <class T> INLINE __device__ void MatrixBase<T>::release() {}
    template <class T> INLINE __device__ void CubeBase<T>::release() {}

    /*
    template <class T> INLINE __device__ void VectorBase<VectorBase<T>
    >::addref() {}
    template <class T> INLINE __device__ void VectorBase<VectorBase<T>
    >::release() {}

    #define IMPL_DYNMEM_FUNC(class_name)	\
            template <class T> INLINE __device__ void VectorBase<VectorBase<T>
    >::addref() {}	\
            template <class T> INLINE __device__ void VectorBase<VectorBase<T>
    >::release() {}

    IMPL_DYNMEM_FUNC(VectorBase<VectorBase<T> >)
    IMPL_DYNMEM_FUNC(VectorBase<MatrixBase<T> >)
    IMPL_DYNMEM_FUNC(VectorBase<CubeBase<T> >)
    IMPL_DYNMEM_FUNC(MatrixBase<VectorBase<T> >)
    IMPL_DYNMEM_FUNC(MatrixBase<MatrixBase<T> >)
    IMPL_DYNMEM_FUNC(MatrixBase<CubeBase<T> >)
    IMPL_DYNMEM_FUNC(CubeBase<VectorBase<T> >)
    IMPL_DYNMEM_FUNC(CubeBase<MatrixBase<T> >)
    IMPL_DYNMEM_FUNC(CubeBase<CubeBase<T> >)
    #undef IMPL_DYNMEM_FUNC
    */
} // namespace quasar

#ifdef TARGET_CUDA
__device__ void *dynmem;
__device__ size_t dynmem_size;
#endif

#endif // ENABLE_DYNAMIC_KERNEL_MEM

//=============================================================================
// Vector casting operations
//=============================================================================

namespace quasar
{

#ifdef CHECK_UNSAFE_BOUNDS
#define CHECK_LENGTH(n)                                                        \
    if (a.dim1 != n)                                                           \
    Q_RAISE_ERROR("Vector dimensions don't match")
#else
#define CHECK_LENGTH(n)
#endif

    // cast from vec to vecX
    INLINE __device__ scalar1 __cast_vec_vec1(Vector a)
    {
        CHECK_LENGTH(1);
        return make_scalar1(a.data[0]);
    }
    INLINE __device__ scalar2 __cast_vec_vec2(Vector a)
    {
        CHECK_LENGTH(2);
        return make_scalar2(a.data[0], a.data[1]);
    }
    INLINE __device__ scalar3 __cast_vec_vec3(Vector a)
    {
        CHECK_LENGTH(3);
        return make_scalar3(a.data[0], a.data[1], a.data[2]);
    }
    INLINE __device__ scalar4 __cast_vec_vec4(Vector a)
    {
        CHECK_LENGTH(4);
        return make_scalar4(a.data[0], a.data[1], a.data[2], a.data[3]);
    }
    template <int N> __device__ scalarN<N> __cast_vec_vecX(Vector a)
    {
        CHECK_LENGTH(N);
        scalarN<N> y;
        for (int i = 0; i < N; i++)
            y.el[i] = a.data[i];
        return y;
    }

    // cast from ivec to ivecX
    INLINE __device__ int1 __cast_ivec_ivec1(VectorBase<int> a)
    {
        CHECK_LENGTH(1);
        return make_int1(a.data[0]);
    }
    INLINE __device__ int2 __cast_ivec_ivec2(VectorBase<int> a)
    {
        CHECK_LENGTH(2);
        return make_int2(a.data[0], a.data[1]);
    }
    INLINE __device__ int3 __cast_ivec_ivec3(VectorBase<int> a)
    {
        CHECK_LENGTH(3);
        return make_int3(a.data[0], a.data[1], a.data[2]);
    }
    INLINE __device__ int4 __cast_ivec_ivec4(VectorBase<int> a)
    {
        CHECK_LENGTH(4);
        return make_int4(a.data[0], a.data[1], a.data[2], a.data[3]);
    }
    template <int N> __device__ intN<N> __cast_vec_vecX(VectorBase<int> a)
    {
        CHECK_LENGTH(N);
        intN<N> y;
        for (int i = 0; i < N; i++)
            y.el[i] = a.data[i];
        return y;
    }


    // cast from cvec to cvecX
    INLINE __device__ cscalar1 __cast_cvec_cvec1(CVector a)
    {
        CHECK_LENGTH(1);
        return make_cscalar1(a.data[0]);
    }
    INLINE __device__ cscalar2 __cast_cvec_cvec2(CVector a)
    {
        CHECK_LENGTH(2);
        return make_cscalar2(a.data[0], a.data[1]);
    }
    INLINE __device__ cscalar3 __cast_cvec_cvec3(CVector a)
    {
        CHECK_LENGTH(3);
        return make_cscalar3(a.data[0], a.data[1], a.data[2]);
    }
    INLINE __device__ cscalar4 __cast_cvec_cvec4(CVector a)
    {
        CHECK_LENGTH(4);
        return make_cscalar4(a.data[0], a.data[1], a.data[2], a.data[3]);
    }
    template <int N> __device__ cscalarN<N> __cast_cvec_cvecX(CVector a)
    {
        CHECK_LENGTH(N);
        cscalarN<N> y;
        for (int i = 0; i < N; i++)
            y.el[i] = a.data[i];
        return y;
    }
#undef CHECK_LENGTH

#ifdef ENABLE_DYNAMIC_KERNEL_MEM

    // cast from vecX to vec
    INLINE __device__ Vector __cast_vec1_vec(scalar1 a)
    {
        Vector b = uninit<scalar>("scalar", 1);
        b.data[0] = a.x;
        return b;
    }
    INLINE __device__ Vector __cast_vec2_vec(scalar2 a)
    {
        Vector b = uninit<scalar>("scalar", 2);
        b.data[0] = a.x;
        b.data[1] = a.y;
        return b;
    }
    INLINE __device__ Vector __cast_vec3_vec(scalar3 a)
    {
        Vector b = uninit<scalar>("scalar", 3);
        b.data[0] = a.x;
        b.data[1] = a.y;
        b.data[2] = a.z;
        return b;
    }
    INLINE __device__ Vector __cast_vec4_vec(scalar4 a)
    {
        Vector b = uninit<scalar>("scalar", 4);
        b.data[0] = a.x;
        b.data[1] = a.y;
        b.data[2] = a.z;
        b.data[3] = a.w;
        return b;
    }
    template <int N> __device__ Vector __cast_vecX_vec(scalarN<N> a)
    {
        Vector b = uninit<scalar>("scalar", N);
        for (int i = 0; i < N; i++)
            b.data[i] = a.el[i];
        return b;
    }

    // cast from cvecX to cvec
    INLINE __device__ CVector __cast_cvec1_cvec(cscalar1 a)
    {
        CVector b = uninit<cscalar>("cscalar", 1);
        b.data[0] = a.x;
        return b;
    }
    INLINE __device__ CVector __cast_cvec2_cvec(cscalar2 a)
    {
        CVector b = uninit<cscalar>("cscalar", 2);
        b.data[0] = a.x;
        b.data[1] = a.y;
        return b;
    }
    INLINE __device__ CVector __cast_cvec3_cvec(cscalar3 a)
    {
        CVector b = uninit<cscalar>("cscalar", 3);
        b.data[0] = a.x;
        b.data[1] = a.y;
        b.data[2] = a.z;
        return b;
    }
    INLINE __device__ CVector __cast_cvec4_cvec(cscalar4 a)
    {
        CVector b = uninit<cscalar>("cscalar", 4);
        b.data[0] = a.x;
        b.data[1] = a.y;
        b.data[2] = a.z;
        b.data[3] = a.w;
        return b;
    }
    template <int N> __device__ CVector __cast_cvecX_cvec(cscalarN<N> a)
    {
        CVector b = uninit<cscalar>("cscalar", N);
        for (int i = 0; i < N; i++)
            b.data[i] = a.el[i];
        return b;
    }

#endif // ENABLE_DYNAMIC_KERNEL_MEM

    //=============================================================================
    // High-level operations
    //=============================================================================

    template <class T, T op(T, T)>
    __device__ T sequential_reduction(VectorBase<T> A)
    {
        T total = A.dim1 == 0 ? T() : A.data[0];
        for (int i = 1; i < A.dim1; i++)
            total = op(total, A.data[i]);
        return total;
    }

    template <class T, T op(T, T)>
    __device__ T sequential_reduction(MatrixBase<T> A)
    {
        T total = A.dim1 == 0 ? T() : A.data[0];
        for (int i = 1; i < A.dim1 * A.dim2; i++)
            total = op(total, A.data[i]);
        return total;
    }

    template <class T, T op(T, T)>
    __device__ T sequential_reduction(CubeBase<T> A)
    {
        T total = A.dim1 == 0 ? T() : A.data[0];
        for (int i = 1; i < A.dim1 * A.dim2 * A.dim3; i++)
            total = op(total, A.data[i]);
        return total;
    }


    template <class T> __device__ T OP_add(T a, T b) { return a + b; }
    template <class T> __device__ T OP_mul(T a, T b) { return a * b; }
    template <class T> __device__ T OP_min(T a, T b) { return min(a, b); }
    template <class T> __device__ T OP_max(T a, T b) { return max(a, b); }

    template <class T> __device__ T sum(VectorBase<T> a)
    {
        return sequential_reduction<T, OP_add>(a);
    }
    template <class T> __device__ T sum(MatrixBase<T> a)
    {
        return sequential_reduction<T, OP_add>(a);
    }
    template <class T> __device__ T sum(CubeBase<T> a)
    {
        return sequential_reduction<T, OP_add>(a);
    }

    template <class T> __device__ T mean(VectorBase<T> a)
    {
        return sequential_reduction<T, OP_add>(a) / a.dim1;
    }
    template <class T> __device__ T mean(MatrixBase<T> a)
    {
        return sequential_reduction<T, OP_add>(a) / (a.dim1 * a.dim2);
    }
    template <class T> __device__ T mean(CubeBase<T> a)
    {
        return sequential_reduction<T, OP_add>(a) / (a.dim1 * a.dim2 * a.dim3);
    }

    template <class T> __device__ T prod(VectorBase<T> a)
    {
        return sequential_reduction<T, OP_mul>(a);
    }
    template <class T> __device__ T prod(MatrixBase<T> a)
    {
        return sequential_reduction<T, OP_mul>(a);
    }
    template <class T> __device__ T prod(CubeBase<T> a)
    {
        return sequential_reduction<T, OP_mul>(a);
    }

    using ::min; // Enable overloading for these functions
    using ::max;

    template <class T> __device__ T min(VectorBase<T> a)
    {
        return sequential_reduction<T, OP_min>(a);
    }
    template <class T> __device__ T min(MatrixBase<T> a)
    {
        return sequential_reduction<T, OP_min>(a);
    }
    template <class T> __device__ T min(CubeBase<T> a)
    {
        return sequential_reduction<T, OP_min>(a);
    }

    template <class T> __device__ T max(VectorBase<T> a)
    {
        return sequential_reduction<T, OP_max>(a);
    }
    template <class T> __device__ T max(MatrixBase<T> a)
    {
        return sequential_reduction<T, OP_max>(a);
    }
    template <class T> __device__ T max(CubeBase<T> a)
    {
        return sequential_reduction<T, OP_max>(a);
    }

    template <class T> __device__ T dotprod(VectorBase<T> A, VectorBase<T> B)
    {
        T total = T();
#ifdef CHECK_UNSAFE_BOUNDS
        if (A.dim1 != B.dim1)
            Q_RAISE_ERROR("Vector dimensions don't match");
#endif
        for (int i = 0; i < A.dim1; i++)
            total += A.data[i] * B.data[i];
        return total;
    }

    template <class T> __device__ T dotprod(MatrixBase<T> A, MatrixBase<T> B)
    {
        T total = T();
#ifdef CHECK_UNSAFE_BOUNDS
        if (A.dim1 != B.dim1 || A.dim2 != B.dim2)
            Q_RAISE_ERROR("Matrix dimensions don't match");
#endif
        for (int i = 0; i < A.dim1 * A.dim2; i++)
            total += A.data[i] * B.data[i];
        return total;
    }

    template <class T> __device__ T dotprod(CubeBase<T> A, CubeBase<T> B)
    {
        T total = T();
#ifdef CHECK_UNSAFE_BOUNDS
        if (A.dim1 != B.dim1 || A.dim2 != B.dim2 || A.dim3 != B.dim3)
            Q_RAISE_ERROR("Cube dimensions don't match");
#endif
        for (int i = 0; i < A.dim1 * A.dim2 * A.dim3; i++)
            total += A.data[i] * B.data[i];
        return total;
    }

#ifdef ENABLE_DYNAMIC_KERNEL_MEM

    // Transpose function
    template <class T>
    __device__ MatrixBase<T> transpose(MatrixBase<T> x, const char *elemT)
    {
        MatrixBase<T> y = uninit<T>(elemT, x.dim2, x.dim1);
        for (int m = 0; m < y.dim2; m++)
            for (int n = 0; n < y.dim1; n++)
                y.data[m * x.dim1 + n] = x.data[n * x.dim2 + m];
        return y;
    }

    // Transpose function for vectors
    template <class T>
    __device__ VectorBase<T> transpose(VectorBase<T> x, const char *elemT)
    {
        VectorBase<T> y = uninit<T>(elemT, x.dim1);
        for (int n = 0; n < x.dim1; n++)
            y.data[n] = x.data[n];
        return y;
    };

    // Hermitian transpose function
    template <class T>
    __device__ MatrixBase<T> herm_transpose(MatrixBase<T> x, const char *elemT)
    {
        MatrixBase<T> y = uninit<T>(elemT, x.dim2, x.dim1);
        for (int m = 0; m < y.dim2; m++)
            for (int n = 0; n < y.dim1; n++)
                y.data[m * x.dim1 + n] = conj(x.data[n * x.dim2 + m]);
        return y;
    }

    // Eye function
    INLINE __device__ Matrix eye(int N)
    {
        Matrix y = uninit<scalar>("scalar", N, N);
        for (int m = 0; m < N; m++)
            for (int n = 0; n < N; n++)
                y.data[m * N + n] = (m == n);
        return y;
    }

    // Copy function
    template <class T>
    __device__ VectorBase<T> copy(VectorBase<T> x, const char *elemT)
    {
        VectorBase<T> y = uninit<T>(elemT, x.dim1);
        for (int m = 0; m < x.dim1; m++)
            y.data[m] = x.data[m];
        return y;
    }

    template <class T>
    __device__ MatrixBase<T> copy(MatrixBase<T> x, const char *elemT)
    {
        MatrixBase<T> y = uninit<T>(elemT, x.dim1, x.dim2);
        for (int m = 0; m < x.dim1 * x.dim2; m++)
            y.data[m] = x.data[m];
        return y;
    }

    template <class T>
    __device__ CubeBase<T> copy(CubeBase<T> x, const char *elemT)
    {
        CubeBase<T> y = uninit<T>(elemT, x.dim1, x.dim2, x.dim3);
        for (int m = 0; m < x.dim1 * x.dim2 * x.dim3; m++)
            y.data[m] = x.data[m];
        return y;
    }

    // Reshape function (all possible combinations)
    template <class T>
    __device__ VectorBase<T> reshape(VectorBase<T> a, int dim1,
                                     const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (dim1 != a.dim1)
            Q_RAISE_ERROR("Reshape: the number of elements must not change!");
#endif
        return copy(a, elemT);
    }

    template <class T>
    __device__ MatrixBase<T> reshape(VectorBase<T> a, int2 dims,
                                     const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (dims.x * dims.y != a.dim1)
            Q_RAISE_ERROR("Reshape: the number of elements must not change!");
#endif
        return MatrixBase<T>(copy(a, elemT).data, dims.x, dims.y);
    }

    template <class T>
    __device__ CubeBase<T> reshape(VectorBase<T> a, int3 dims,
                                   const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (dims.x * dims.y * dims.z != a.dim1)
            Q_RAISE_ERROR("Reshape: the number of elements must not change!");
#endif
        return CubeBase<T>(copy(a, elemT).data, dims.x, dims.y, dims.z);
    }

    template <class T>
    __device__ VectorBase<T> reshape(MatrixBase<T> a, int dim1,
                                     const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (dim1 != a.dim1 * a.dim2)
            Q_RAISE_ERROR("Reshape: the number of elements must not change!");
#endif
        return VectorBase<T>(copy(a, elemT).data, dim1);
    }

    template <class T>
    __device__ VectorBase<T> reshape(CubeBase<T> a, int dim1, const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (dim1 != a.dim1 * a.dim2 * a.dim3)
            Q_RAISE_ERROR("Reshape: the number of elements must not change!");
#endif
        return VectorBase<T>(copy(a, elemT).data, dim1);
    }

    template <class T>
    __device__ MatrixBase<T> reshape(MatrixBase<T> a, int2 dims,
                                     const char *elemT)
    {
        return reshape(reshape(a, a.dim1 * a.dim2, elemT), dims, elemT);
    }
    template <class T>
    __device__ MatrixBase<T> reshape(CubeBase<T> a, int2 dims,
                                     const char *elemT)
    {
        return reshape(reshape(a, a.dim1 * a.dim2 * a.dim3, elemT), dims,
                       elemT);
    }
    template <class T>
    __device__ CubeBase<T> reshape(MatrixBase<T> a, int3 dims,
                                   const char *elemT)
    {
        return reshape(reshape(a, a.dim1 * a.dim2, elemT), dims, elemT);
    }
    template <class T>
    __device__ CubeBase<T> reshape(CubeBase<T> a, int3 dims, const char *elemT)
    {
        return reshape(reshape(a, a.dim1 * a.dim2 * a.dim3, elemT), dims,
                       elemT);
    }


    // Squeeze function
    template <class T>
    __device__ VectorBase<T> squeeze(VectorBase<T> x, const char *elemT)
    {
        return x;
    }

    template <class T>
    __device__ MatrixBase<T> squeeze(MatrixBase<T> x, const char *elemT)
    {
        return (x.dim1 == 1) ? reshape(x, make_int2(x.dim2, 1), elemT) : x;
    }

    template <class T>
    __device__ CubeBase<T> squeeze(CubeBase<T> x, const char *elemT)
    {
        int dims[3] = {x.dim1, x.dim2, x.dim3};
        int j = 0;
        for (int i = 0; i < 3; i++)
        {
            dims[j] = dims[i];
            if (dims[j] > 1)
                j++;
        }
        for (; j < 3; j++)
            dims[j] = 1;

        return reshape(x, make_int3(dims[0], dims[1], dims[2]), elemT);
    };

    // Repmat function
    template <class T>
    __device__ CubeBase<T> repmat(CubeBase<T> x, int3 dims, const char *elemT)
    {
        CubeBase<T> y =
            uninit<T>(elemT, x.dim1 * dims.x, x.dim2 * dims.y, x.dim3 * dims.z);
        for (int m = 0; m < y.dim1; m++)
            for (int n = 0; n < y.dim2; n++)
                for (int p = 0; p < y.dim3; p++)
                    y.data[(n + m * y.dim2) * y.dim3 + p] =
                        x.data[((m % x.dim1) * x.dim2 + (n % x.dim2)) * x.dim3 +
                               (p % x.dim3)];
        return y;
    }

    template <class T>
    __device__ MatrixBase<T> repmat(MatrixBase<T> x, int2 dims,
                                    const char *elemT)
    {
        MatrixBase<T> y = uninit<T>(elemT, x.dim1 * dims.x, x.dim2 * dims.y);
        for (int m = 0; m < y.dim1; m++)
            for (int n = 0; n < y.dim2; n++)
                y.data[n + m * y.dim2] =
                    x.data[(m % x.dim1) * x.dim2 + (n % x.dim2)];
        return y;
    }

    template <class T>
    __device__ VectorBase<T> repmat(VectorBase<T> x, int dims,
                                    const char *elemT)
    {
        VectorBase<T> y = uninit<T>(elemT, x.dim1 * dims);
        for (int m = 0; m < y.dim1; m++)
            y.data[m] = x.data[m % x.dim1];
        return y;
    }

    template <class T>
    __device__ MatrixBase<T> repmat(VectorBase<T> x, int2 dims,
                                    const char *elemT)
    {
        return repmat(reshape(x, make_int2(1, x.dim1)), dims, elemT);
    }

    template <class T>
    __device__ CubeBase<T> repmat(VectorBase<T> x, int3 dims, const char *elemT)
    {
        return repmat(reshape(x, make_int3(1, x.dim1, 1)), dims, elemT);
    }

    template <class T>
    __device__ CubeBase<T> repmat(MatrixBase<T> x, int3 dims, const char *elemT)
    {
        return repmat(reshape(x, make_int3(x.dim1, x.dim2, 1)), dims, elemT);
    }

    // Shuffledims function
    template <class T>
    __device__ MatrixBase<T> shuffledims(MatrixBase<T> x, int3 newDim,
                                         const char *elemT)
    {
        // Two possibilities
        if (newDim.x == 0 && newDim.y == 0)
            return copy(x, elemT);
        else
            return transpose(x, elemT);
    }

    template <class T>
    __device__ CubeBase<T> shuffledims(CubeBase<T> x, int3 newDim,
                                       const char *elemT)
    {
        int dims[3] = {x.dim1, x.dim2, x.dim3};
        int dim_stride[3] = {x.dim2 * x.dim3, x.dim3, 1};
        CubeBase<T> y =
            uninit<T>(elemT, dims[newDim.x], dims[newDim.y], dims[newDim.z]);
        int3 invDim = make_int3(newDim.x == 0 ? 0 : newDim.y == 0 ? 1 : 2,
                                newDim.x == 1 ? 0 : newDim.y == 1 ? 1 : 2,
                                newDim.x == 2 ? 0 : newDim.y == 2 ? 1 : 2);
        int dM = dim_stride[invDim.x];
        int dN = dim_stride[invDim.y];
        int dK = dim_stride[invDim.z];

        for (int m = 0; m < x.dim1; m++)
            for (int n = 0; n < x.dim2; n++)
                for (int k = 0; k < x.dim3; k++)
                {
                    y.data[m * dM + n * dN + k * dK] =
                        x.data[(m * x.dim2 + n) * x.dim3 + k];
                }
        return y;
    }

    // Matrix multiplication
    template <class T>
    __device__ MatrixBase<T> mmult(MatrixBase<T> a, MatrixBase<T> b,
                                   const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (a.dim2 != b.dim1)
            Q_RAISE_ERROR("Matrix multiplication: the dimensions must match!");
#endif
        MatrixBase<T> c = uninit<T>(elemT, a.dim1, b.dim2);
        for (int m = 0; m < a.dim1; m++)
            for (int n = 0; n < b.dim2; n++)
            {
                T sum = T();
                for (int k = 0; k < a.dim2; k++)
                {
                    sum += a.data[m * a.dim2 + k] * b.data[k * b.dim2 + n];
                }
                c.data[m * c.dim2 + n] = sum;
            }
        return c;
    }

    // Matrix-vector multiplication
    template <class T>
    __device__ VectorBase<T> mvmult(MatrixBase<T> a, VectorBase<T> b,
                                    const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (a.dim2 != b.dim1)
            Q_RAISE_ERROR("Matrix multiplication: the dimensions must match!");
#endif
        VectorBase<T> c = uninit<T>(elemT, a.dim1);
        for (int m = 0; m < a.dim1; m++)
        {
            T sum = T();
            for (int k = 0; k < a.dim2; k++)
                sum += a.data[m * a.dim2 + k] * b.data[k];
            c.data[m] = sum;
        }
        return c;
    }

    // Vector-matrix multiplication
    template <class T>
    __device__ VectorBase<T> vmmult(VectorBase<T> a, MatrixBase<T> b,
                                    const char *elemT)
    {
#ifdef CHECK_UNSAFE_BOUNDS
        if (a.dim1 != b.dim2)
            Q_RAISE_ERROR("Matrix multiplication: the dimensions must match!");
#endif
        VectorBase<T> c = uninit<T>(elemT, b.dim2);
        for (int m = 0; m < b.dim2; m++)
        {
            T sum = T();
            for (int k = 0; k < a.dim1; k++)
                sum += a.data[k] * b.data[k * b.dim2 + m];
            c.data[m] = sum;
        }
        return c;
    }


    // Seq function
    INLINE __device__ Vector seq(scalar a, scalar b, scalar step)
    {
        int count = (int)floor((b - a) / step + 1);
        Vector y = uninit<scalar>("scalar", count);
        for (int n = 0; n < count; n++)
            y.data[n] = a + n * step;
        return y;
    }

    // Seq function
    INLINE __device__ Vector seq(int a, int b, int step)
    {
        int count = step == 1 ? b - a + 1 : ((b - a) / step + 1);
        Vector y = uninit<scalar>("scalar", count);
        for (int n = 0; n < count; n++)
            y.data[n] = a + n * step;
        return y;
    }

    // Linspace function
    INLINE __device__ Vector linspace(scalar a, scalar b, int count)
    {
        Vector y = uninit<scalar>("scalar", count);
        scalar delta = b - a;
        if (count == 1)
            y.data[0] = a;
        else
        {
            for (int n = 0; n < count;
                 n++) // Note: division because it is the most accurate
                y.data[n] = a + (delta * (scalar)n) / (count - 1);
        }
        return y;
    }


// TODO: sum/prod/min/max along one dimension
// TODO: cumsum/cumprod
// TODO: vertcat

#endif // ENABLE_DYNAMIC_KERNEL_MEM    

#ifndef TARGET_CUDA

    // Runtime load balancer for OpenMP: determines the number of CPU threads
    // based on the data dimensions and the kernel complexity.
    INLINE __device__ int runtime_loadbalancer(int3 blkDim, int3 gridDim,
                                               int complexity_level)
    {
        int total = prod(gridDim * blkDim);
        // data dimensions too small or the complexity level too low...
        if ((total < 32768 && complexity_level < 5) || total < 4096)
        {
            return 1;
        }
#ifdef _OPENMP
        return omp_get_max_threads();
#else
        // Compiling without OpenMP...
        return 1;
#endif
    }

#else

    INLINE __device__ int cuda_calculate_blkdim(int dims)
    {
        return min(1024, NextPow2(dims));
    }

    INLINE __device__ int2 cuda_calculate_blkdim(int2 dims)
    {
        int x = NextPow2(dims.x);
        int y = NextPow2(dims.y);
        while (x * y > 32 && y > 1)
            y /= 2;
        while (x * y > 32 && x > 1)
            x /= 2;
        return make_int2(x, y);
    }

    INLINE __device__ int3 cuda_calculate_blkdim(int3 dims)
    {
        int x = NextPow2(dims.x);
        int y = NextPow2(dims.y);
        int z = NextPow2(dims.z);
        while (x * y * z > 32 && z > 1)
            z /= 2;
        while (x * y * z > 32 && y > 1)
            y /= 2;
        while (x * y * z > 32 && x > 1)
            x /= 2;
        return make_int3(x, y, z);
    }

    INLINE __device__ dim3 make_dim3(int dim) { return dim3(dim, 1, 1); }
    INLINE __device__ dim3 make_dim3(int2 dim) { return dim3(dim.x, dim.y, 1); }
    INLINE __device__ dim3 make_dim3(int3 dim)
    {
        return dim3(dim.x, dim.y, dim.z);
    }


#endif

} // namespace quasar

#endif
