#ifndef __QUASAR_HOST_H__
#define __QUASAR_HOST_H__

#include "quasar.h"
#include "quasar_ncube.h"
#include <wchar.h>

#if defined(_MSC_VER)
#include <crtdbg.h>
#else
#include <cassert>
#define _ASSERT assert
#endif


typedef const void *LPCVOID;
typedef void *LPVOID;
typedef const wchar_t *LPCWSTR;
typedef wchar_t *LPWSTR;
typedef wchar_t TCHAR;
#define interface struct

namespace quasar
{
    // Quasar internal data types
    enum DataTypes
    {
        TYPE_VOID,
        TYPE_SCALAR, // A scalar number (single or double precision, depending
                     // on whether DBL_SCALAR is defined)
        TYPE_COMPLEXSCALAR, // A complex-valued scalar number
        TYPE_INT,           // 32-bit integer
        TYPE_VEC,           // Variable length vector (of type 'vec')
        TYPE_MAT,           // Variable size matrix (of type 'mat')
        TYPE_CUBE,          // Variable size cube (of type 'cube')
        TYPE_CVEC,          // Variable length complex vector (of type 'cvec')
        TYPE_CMAT,          // Variable size complex matrix (of type 'cmat')
        TYPE_CCUBE,         // Variable size complex cube (of type 'ccube')
        TYPE_STRING,        // Variable length string
        TYPE_TYPEINFO,      // Type information
        TYPE_LAMBDAEXPR,    // Lambda expression
        TYPE_INT8,
        TYPE_INT16,
        TYPE_INT64,
        TYPE_UINT8,
        TYPE_UINT16,
        TYPE_UINT32,
        TYPE_UINT64,
        TYPE_TYPEDOBJECT,
        TYPE_UNTYPEDOBJECT,
		TYPE_NCUBE,			// Variable dimension & size cube
		TYPE_NCCUBE,		// Variable dimension & size complex cube
        TYPE_METHOD,
        TYPE_COUNT,

        TYPE_INT32 = TYPE_INT,
    };

    // Flags for locking arrays/matrices etc.
    enum LockingModes
    {
        LOCK_READ = 1,
        LOCK_WRITE = 2,
        LOCK_READWRITE = 3
    };

    // Memory resource flags (for identifying the resource that has to be
    // locked)
    enum MemResources
    {
        MEMRESOURCE_MANAGED = 0,
        MEMRESOURCE_CPU = 1,
        MEMRESOURCE_SINGLE_CUDA =
            2, // Selects the CUDA memory resource (if available)
        MEMRESOURCE_SINGLE_OPENCL =
            3 // Selects the OpenCL memory resource (if available)
    };

    // The results of a locking operation
    enum LockResults
    {
        LOCKRESULT_OK = 0,
        LOCKRESULT_RES_NOT_AVAILABLE = 1,
        LOCKRESULT_OUT_OF_MEM = 2,
		LOCKRESULT_INUSE = 3,
        LOCKRESULT_INVALID
    };

    // Flags for allocating memory
    enum AllocationFlags
    {
        ALLOCATIONFLAGS_NONE = 0,
        ALLOCATIONFLAGS_FORCEGPULOAD =
            2, // Forces the memory to be transferred to the (primary) GPU
        ALLOCATIONFLAGS_FORCECPULOAD =
            4 // Forces the memory to be transferred to the (primary) CPU
    };

    // Represents the data type used for 'scalar' in Quasar
    enum ScalarTypes
    {
        SCALARTYPE_SINGLE = 0, // 32-bit single precision floating point
        SCALARTYPE_DOUBLE,     // 64-bit double precision floating point
    };

    // Numerical operations in Quasar
    enum OperatorTypes
    {
        OP_ADD = 0,              // addition
        OP_SUB = 1,              // subtraction
        OP_MULTIPLY = 2,         // multiplication (matrix multiplication)
        OP_DIVIDE = 3,           // divide
        OP_RDIVIDE = 4,          // right division
        OP_POW = 5,              // power
        OP_PW_MULTIPLY = 6,      // point-wise multiplication
        OP_PW_DIVIDE = 7,        // point-wise division
        OP_PW_POW = 8,           // point-wise power
        OP_LESS = 9,             // less than
        OP_LESSOREQ = 10,        // less than or equal
        OP_GREATER = 11,         // greater
        OP_GREATEROREQ = 12,     // greater or equal
        OP_EQUAL = 13,           // equal
        OP_NOTEQUAL = 14,        // not equal
        OP_ASSIGN = 15,          // assignment
        OP_NEGATE = 16,          // negation (-)
        OP_INVERT = 17,          // logical inversion (!)
        OP_LOG_AND = 18,         // logical AND
        OP_LOG_OR = 19,          // logical or
        OP_DOTDOT = 20,          // sequence (a..b)
        OP_DOTDOTDOT = 21,       // sequence with step (a..b..c)
        OP_ADD_ASSIGN = 24,      // inplace assignment +=
        OP_SUB_ASSIGN = 25,      // inplace subtraction -=
        OP_MULTIPLY_ASSIGN = 26, // inplace multiplication *=
        OP_DIVIDE_ASSIGN = 27,   // inplace division /=
        OP_RDIVIDE_ASSIGN =
            28, // inplace left multiplication \= (reserved for future use)
        OP_POW_ASSIGN = 29, // inplace power ^=
        OP_PW_MULTIPLY_ASSIGN = 30,
        OP_PW_DIVIDE_ASSIGN = 31,
        OP_PW_POW_ASSIGN = 32,
        OP_XOR_ASSIGN = 33,
        OP_OR_ASSIGN = 34,
        OP_AND_ASSIGN = 35,
        OP_MIN_ASSIGN = 36,
        OP_MAX_ASSIGN = 37,
        OP_COND_IF = 38,        // conditional IF ? :
        OP_PIPELINE_RIGHT = 41, // reserved operator |>
        OP_PIPELINE_LEFT = 42,  // reserved operator <|
    };

    // Modes for handling the array/matrix/cube boundaries
    enum BoundaryAccessMode
    {
        Default = 0,   // Raise an exception (checked)
        Unchecked = 1, // Fastest option - no checking
        Zero = 2,      // Boundary extension with zeros
        Circular = 3,  // Circular extension
        Mirror = 4,    // Mirrored extension
        Clamp = 5,     // Clamps to the border value
    };

    enum ProfilingModes
    {
        PROFILE_EXECUTIONTIME,
        PROFILE_MEMLEAKS,
        PROFILE_ACCURACY
    };

	enum HostProperties
	{
		OPENCL_CURRENT_CONTEXT = 0x1000,
		OPENCL_COMMANDQUEUE = 0x1001
	};

    typedef void *qhandle; // internal quasar handle

    //
    // String type
    //
    struct string_t
    {
        int length;
        const wchar_t *bytes;

        string_t() {}
        string_t(const wchar_t *bytes, int length)
            : bytes(bytes), length(length)
        {
        }
    };

    //
    // QValue types (24 bytes - 32-bit / 40 bytes - 64-bit)
    //
    struct qvalue_t
    {
        enum DataTypes type; // The type of this QValue object
        qhandle private_obj; // Internal object - don't use!
        union
        {
            scalar scalarVal;
            struct
            {
                scalar realVal;
                scalar imagVal;
            };
            int intVal;
            struct
            {
                void *ptrVal;
                int dim1;
                int dim2;
                int dim3;
            };
        };

		static qvalue_t fromInt(int v)
		{
			qvalue_t t;
			t.type = TYPE_INT;
			t.intVal = v;
			return t;
		}

        static qvalue_t fromScalar(scalar v)
        {
            qvalue_t t;
            t.type = TYPE_SCALAR;
            t.scalarVal = v;
            return t;
        }
        static qvalue_t fromCScalar(cscalar v)
        {
            qvalue_t t;
            t.type = TYPE_COMPLEXSCALAR;
            t.realVal = v.x;
            t.imagVal = v.y;
            return t;
        }
        qvalue_t() { type = TYPE_VOID; private_obj = NULL; }
    };

    //
    // Lambda expression with variable number of input/output arguments
    //
    typedef void (*LambdaDelegate)(qvalue_t *argsIn, int nArgsIn,
                                   qvalue_t *argsOut, int nArgsOut);

    //
    // Custom trait for storing type information about Quasar data types
    //
    template <typename T> struct typeinfo_t
    {
        static const DataTypes type = TYPE_COUNT;
        static LPCWSTR typeName;
    };

#define DECLARE_TYPE(quasarTypeCode, nativeType, quasarTypeName)               \
    template <> struct typeinfo_t<nativeType>                                  \
    {                                                                          \
        static const DataTypes type = quasarTypeCode;                          \
        static LPCWSTR typeName;                                               \
    };
#define IMPLEMENT_TYPE(quasarTypeCode, nativeType, quasarTypeName)             \
    LPCWSTR typeinfo_t<nativeType>::typeName = quasarTypeName;

#define DECLARE_STRUCT_TYPE(nativeType)                                        \
    DECLARE_TYPE(TYPE_TYPEDOBJECT, nativeType, L#nativeType)

//>>Quasar type declarations
    DECLARE_TYPE(TYPE_SCALAR, scalar, L"scalar");
    DECLARE_TYPE(TYPE_COMPLEXSCALAR, cscalar, L"cscalar");
    DECLARE_TYPE(TYPE_INT, int, L"int");
    DECLARE_TYPE(TYPE_VEC, Vector, L"vec");
    DECLARE_TYPE(TYPE_MAT, Matrix, L"mat");
    DECLARE_TYPE(TYPE_CUBE, Cube, L"cube");
    DECLARE_TYPE(TYPE_CVEC, CVector, L"cvec");
    DECLARE_TYPE(TYPE_CMAT, CMatrix, L"cmat");
    DECLARE_TYPE(TYPE_CCUBE, CCube, L"ccube");
    DECLARE_TYPE(TYPE_STRING, string_t, L"string");
    DECLARE_TYPE(TYPE_INT8, int8_t, L"int8");
    DECLARE_TYPE(TYPE_INT16, int16_t, L"int16");
    DECLARE_TYPE(TYPE_INT64, int64_t, L"int64");
    DECLARE_TYPE(TYPE_UINT8, uint8_t, L"uint8");
    DECLARE_TYPE(TYPE_UINT16, uint16_t, L"uint16");
    DECLARE_TYPE(TYPE_UINT32, uint32_t, L"uint32");
    DECLARE_TYPE(TYPE_UINT64, uint64_t, L"uint64");
    DECLARE_TYPE(TYPE_VOID, qvalue_t, L"??");

	DECLARE_TYPE(TYPE_NCUBE, NCube<4>, L"cube{4}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<5>, L"cube{5}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<6>, L"cube{6}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<7>, L"cube{7}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<8>, L"cube{8}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<9>, L"cube{9}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<10>, L"cube{10}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<11>, L"cube{11}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<12>, L"cube{12}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<13>, L"cube{13}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<14>, L"cube{14}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<15>, L"cube{15}");
	DECLARE_TYPE(TYPE_NCUBE, NCube<16>, L"cube{16}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<4>, L"ccube{4}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<5>, L"ccube{5}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<6>, L"ccube{6}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<7>, L"ccube{7}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<8>, L"ccube{8}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<9>, L"ccube{9}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<10>, L"ccube{10}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<11>, L"ccube{11}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<12>, L"ccube{12}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<13>, L"ccube{13}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<14>, L"ccube{14}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<15>, L"ccube{15}");
	DECLARE_TYPE(TYPE_NCCUBE, NCCube<16>, L"ccube{16}");
//<<End Quasar type declarations

#define VERIFY_LOCK(x) (x)

    interface IRefCountable
    {
        virtual void AddRef() = 0;
        virtual void Release() = 0;
    };

    interface IComputationEngine;
    interface IEvaluationStack;
    interface IRuntimeReductionEngine;
    interface ITypeEnvironment;

    //======================================================================
    // IQuasarHost - allows you to communicate with the Quasar host. It is
    // possible to load Quasar modules, call functions, read and write
    // variable values. There are also some utility functions for message
    // loop handling etc.
    //======================================================================
    interface IQuasarHost : public IRefCountable
    {
        // virtual bool OpenDevice(LPCWSTR deviceName, LPCWSTR *errorMsg) = 0;
        // virtual bool OpenDeviceFromXml(LPCWSTR xmlString, LPCWSTR *errorMsg)
        // = 0;
        // virtual bool ListDevices(LPSTR** deviceNames, int maxEntries, int
        // maxEntryLength) = 0;

		// TODO: add support for higher dimensional matrix types

        virtual bool LoadSourceModule(LPCWSTR sourceModule,
                                      LPCWSTR * errorMsg) = 0;
        virtual bool LoadBinaryModule(LPCWSTR binaryModule,
                                      LPCWSTR * errorMsg) = 0;
        virtual bool LoadModuleFromSource(
            LPCWSTR moduleName, LPCWSTR sourceString, LPCWSTR * errorMsg) = 0;
		virtual bool UnloadModule(LPCWSTR moduleName) = 0;
        virtual bool FunctionExists(LPCWSTR functionName) = 0;
        virtual bool FunctionCall(LPCWSTR functionName, const qvalue_t *inArgs,
                                  int nInArgs, qvalue_t *outArgs,
                                  int nOutArgs) = 0;
        virtual bool FunctionCall(const qvalue_t &function,
                                  const qvalue_t *inArgs, int nInArgs,
                                  qvalue_t *outArgs, int nOutArgs) = 0;
        virtual qvalue_t CreateVector(qhandle elemType, int numel) = 0;
        virtual qvalue_t CreateMatrix(qhandle elemType, int dim1, int dim2) = 0;
        virtual qvalue_t CreateCube(qhandle elemType, int dim1, int dim2,
                                    int dim3) = 0;
		virtual qvalue_t CreateNCube(qhandle elemType, int ndims, const int* dims) = 0;
        virtual qvalue_t CreateTypedObject(qhandle objType) = 0;
        virtual qvalue_t CreateUntypedObject(qhandle objType) = 0;
        virtual qvalue_t CreateString(LPCWSTR text, int length = -1) = 0;
        virtual qvalue_t CreateLambda(qhandle functionTypeHandle,
                                      LambdaDelegate lambda) = 0;
		virtual void GetNDims(const qvalue_t &obj, int *ndims, int *dims) = 0;
        virtual void AddRef(qvalue_t & obj) = 0;
        virtual void ReleaseRef(qvalue_t & obj) = 0;
        virtual void DeleteValue(qvalue_t & obj) = 0;
        virtual LockResults Lock(const qvalue_t &obj, DataTypes elemType,
                                 LockingModes lockingMode,
                                 MemResources memResource, void **pData) = 0;
        virtual void Unlock(const qvalue_t &obj, LockingModes lockingMode,
                            MemResources memResource) = 0;
        virtual qhandle GetPrimitiveTypeHandle(DataTypes type) = 0;
        virtual void RunApp() = 0;
        virtual void DoEvents() = 0;
        virtual bool ReadVariable(LPCWSTR varName, qvalue_t & val) = 0;
        virtual bool WriteVariable(LPCWSTR varName, const qvalue_t &val) = 0;
        virtual bool GetField(const qvalue_t &obj, LPCWSTR fieldName,
                              qvalue_t &val) = 0;
        virtual bool SetField(const qvalue_t &obj, LPCWSTR fieldName,
                              const qvalue_t &val) = 0;
        virtual qvalue_t LookupFunction(LPCWSTR functionSignature) = 0;
        virtual qvalue_t LookupType(LPCWSTR quasarTypeName) = 0;
        virtual qvalue_t LookupMethod(const qvalue_t &type,
                                      LPCWSTR functionSignature) = 0;
        virtual bool MethodCall(const qvalue_t &function,
                                const qvalue_t &target, const qvalue_t *inArgs,
                                int nInArgs, qvalue_t *outArgs,
                                int nOutArgs) = 0;
        virtual qvalue_t GetType(const qvalue_t &obj) = 0;
        virtual qvalue_t CreateType(LPCWSTR moduleName, LPCWSTR typeName) = 0;
        virtual bool AddField(qhandle objType, LPCWSTR fieldName,
                              qhandle fieldType) = 0;
        virtual qvalue_t AddParameter(qhandle objType, LPCWSTR paramName) = 0;

        virtual void FinalizeType(qhandle typeHandle) = 0;
        virtual bool EnableProfiling(ProfilingModes profilingMode,
                                     LPCWSTR outputFileName = NULL) = 0;
        virtual IEvaluationStack *CreateStack() = 0;
		virtual bool QueryProperty(HostProperties prop, int param, void** dst, size_t nBytes) = 0;
        virtual IComputationEngine *GetComputationEngine() = 0;

        //=========================================================
        // Generic helper functions
        //=========================================================
        template <typename T> qhandle GetTypeHandle()
        {
            if (typeinfo_t<T>::type == TYPE_TYPEDOBJECT)
            {
                // If you get an error at this line, then you probably forgot to
                // define the macro DECLARE_STRUCT_TYPE() for your user-defined
                // type!
                return LookupType(typeinfo_t<T>::typeName).private_obj;
            }
            return GetPrimitiveTypeHandle(typeinfo_t<T>::type);
        }

        // Creation of generic vectors
        template <typename T> qvalue_t CreateVector(int numel)
        {
            return CreateVector(GetTypeHandle<T>(), numel);
        }

        // Creation of generic matrices
        template <typename T> qvalue_t CreateMatrix(int dim1, int dim2)
        {
            return CreateMatrix(GetTypeHandle<T>(), dim1, dim2);
        }

        // Creation of generic cubes
        template <typename T> qvalue_t CreateCube(int dim1, int dim2, int dim3)
        {
            return CreateCube(GetTypeHandle<T>(), dim1, dim2, dim3);
        }

	    // Creation of generic n-cubes (higher dimensional cubes)
        template <typename T> qvalue_t CreateNCube(int ndims, const int *dims)
        {
            return CreateNCube(GetTypeHandle<T>(), ndims, dims);
        }

		template <typename T, int N> qvalue_t CreateNCube(intN<N> dims)
		{
			return CreateNCube(GetTypeHandle<T>(), N, (int *) &dims);
		}

        // Creation of initialized generic vectors
        template <typename T> qvalue_t CreateVector(int numel, const T *vals)
        {
            qvalue_t vec = CreateVector<T>(numel);
            VectorBase<T> v = LockVector<T>(vec);
            memcpy(v.data, vals, numel * sizeof(T));
            UnlockVector(vec);
            return vec;
        }
        template <typename T, int N> qvalue_t CreateVector(T(&vals)[N])
        {
            return CreateVector(N, vals);
        }

        template <typename T>
        qvalue_t CreateMatrix(int dim1, int dim2, const T *vals)
        {
            qvalue_t mat = CreateMatrix<T>(dim1, dim2);
            MatrixBase<T> m = LockMatrix<T>(mat);
            memcpy(m.data, vals, dim1 * dim2 * sizeof(T));
            UnlockMatrix(mat);
            return mat;
        }


        template <typename T>
        VectorBase<T> LockVector(const qvalue_t &obj,
                                 LockingModes lockingMode = LOCK_READWRITE,
                                 MemResources memResource = MEMRESOURCE_CPU)
        {
            LPVOID ptr = NULL;
            VERIFY_LOCK(
                Lock(obj, typeinfo_t<T>::type, lockingMode, memResource, &ptr));
            return VectorBase<T>((T *)ptr, obj.dim1);
        }
        void UnlockVector(const qvalue_t &obj,
                          LockingModes lockingMode = LOCK_READWRITE,
                          MemResources memResource = MEMRESOURCE_CPU)
        {
            Unlock(obj, lockingMode, memResource);
        }
        template <typename T>
        MatrixBase<T> LockMatrix(const qvalue_t &obj,
                                 LockingModes lockingMode = LOCK_READWRITE,
                                 MemResources memResource = MEMRESOURCE_CPU)
        {
            LPVOID ptr = NULL;
            VERIFY_LOCK(
                Lock(obj, typeinfo_t<T>::type, lockingMode, memResource, &ptr));
            return MatrixBase<T>((T *)ptr, obj.dim1, obj.dim2);
        }
        void UnlockMatrix(const qvalue_t &obj,
                          LockingModes lockingMode = LOCK_READWRITE,
                          MemResources memResource = MEMRESOURCE_CPU)
        {
            Unlock(obj, lockingMode, memResource);
        }
        template <typename T>
        CubeBase<T> LockCube(const qvalue_t &obj,
                             LockingModes lockingMode = LOCK_READWRITE,
                             MemResources memResource = MEMRESOURCE_CPU)
        {
            LPVOID ptr = NULL;
            VERIFY_LOCK(
                Lock(obj, typeinfo_t<T>::type, lockingMode, memResource, &ptr));
            return CubeBase<T>((T *)ptr, obj.dim1, obj.dim2, obj.dim3);
        }
        void UnlockCube(const qvalue_t &obj,
                        LockingModes lockingMode = LOCK_READWRITE,
                        MemResources memResource = MEMRESOURCE_CPU)
        {
            Unlock(obj, lockingMode, memResource);
        }
		template <typename T, int N>
        NCubeBase<T, N> LockNCube(const qvalue_t &obj,
                             LockingModes lockingMode = LOCK_READWRITE,
                             MemResources memResource = MEMRESOURCE_CPU)
        {
            LPVOID ptr = NULL;
			intN<N> dims;
			int ndims = N;
			// Get the dimensions
			GetNDims(obj, &ndims, (int *) &dims);
			_ASSERT(ndims == N); // Number of dimensions do not match!
            VERIFY_LOCK(
                Lock(obj, typeinfo_t<T>::type, lockingMode, memResource, &ptr));
            return NCubeBase<T, N>((T *)ptr, dims);
        }
        void UnlockNCube(const qvalue_t &obj,
                        LockingModes lockingMode = LOCK_READWRITE,
                        MemResources memResource = MEMRESOURCE_CPU)
        {
            Unlock(obj, lockingMode, memResource);
        }
        template <typename T>
        T *LockObject(const qvalue_t &obj,
                      LockingModes lockingMode = LOCK_READWRITE,
                      MemResources memResource = MEMRESOURCE_CPU)
        {
            LPVOID ptr = NULL;
            _ASSERT(obj.type == TYPE_TYPEDOBJECT);
            VERIFY_LOCK(
                Lock(obj, TYPE_TYPEDOBJECT, lockingMode, memResource, &ptr));
            return (T *)ptr;
        }
        void UnlockObject(const qvalue_t &obj,
                          LockingModes lockingMode = LOCK_READWRITE,
                          MemResources memResource = MEMRESOURCE_CPU)
        {
            Unlock(obj, lockingMode, memResource);
        }

        static IQuasarHost *Create(LPCWSTR deviceName, bool loadCompiler = true);
        static IQuasarHost *Connect(LPCVOID hostprivParams,
                                    LPCVOID moduleDetails, int flags);
        static IQuasarHost *GetInstance();
    };

    //======================================================================
    // IEvaluationStack - gives access to Quasar's internal evaluation stack
    //======================================================================
    interface IEvaluationStack : public IRefCountable
    {
        virtual qvalue_t PopValue() = 0;
        virtual void PushValue(const qvalue_t &value) = 0;
        virtual void PopMultiple(qvalue_t * vals, int count) = 0;
        virtual void PushMultiple(const qvalue_t *vals, int count) = 0;
        virtual void Clear() = 0;
        virtual void PushContext() = 0;
        virtual void PopContext() = 0;
        virtual void Process(OperatorTypes op) = 0;
        virtual void FunctionCall(LPCWSTR functionName, int numArgs) = 0;
        virtual void ArrayGetAt(const qvalue_t &x, int numIndices,
                                BoundaryAccessMode boundsMode) = 0;
        virtual void ArraySetAt(qvalue_t & x, int numIndices,
                                BoundaryAccessMode boundsMode) = 0;
        virtual qvalue_t Evaluate(const qvalue_t &x) = 0;
        virtual int GetCount() = 0;
    };

    //======================================================================
    // IMatrixFactory - provides helper routines for creating matrices
    //======================================================================
    interface IMatrixFactory : public IRefCountable
    {
        virtual qvalue_t New(AllocationFlags flags, LPCVOID elems, int nDims,
                             const int *dims) = 0;
    };

    //======================================================================
    // ITypeEnvironment - provides access to the type information for
    // certain types.
    //======================================================================
    interface ITypeEnvironment : public IRefCountable
    {
        virtual ScalarTypes GetScalarType() = 0;
    };

    //======================================================================
    // IRuntimeReductionEngine - provides access to the runtime reduction
    // engine. Allows you to define/undefine your own reductions that are
    // then applied at run-time.
    //======================================================================
    interface IRuntimeReductionEngine : public IRefCountable
    {
        virtual bool Add(LPWSTR searchExpr, LambdaDelegate action,
                         LambdaDelegate whereClause) = 0;
        virtual bool Remove(LPWSTR searchExpr) = 0;
    };

    //======================================================================
    // IComputationEngine - this interface represents the core of the Quasar
    // runtime system, by creating a uniform interface to the underlying
    // computation device
    //======================================================================
    interface IComputationEngine : public IRefCountable
    {
        virtual bool GetName(LPWSTR name, int maxChars) = 0;
        virtual IEvaluationStack *GetEvaluationStack() = 0;
        virtual IMatrixFactory *GetMatrixFactory() = 0;
        virtual ITypeEnvironment *GetTypeEnvironment() = 0;
        virtual IRuntimeReductionEngine *GetRuntimeReductionEngine() = 0;
        virtual void Process(OperatorTypes op) = 0;
        virtual void ConstructMatrix(int numRowsOrCols) = 0;
        virtual void ConstructCellMatrix(int length) = 0;
        virtual bool FunctionCall(LPCWSTR functionName, int numArgs) = 0;
        virtual bool FunctionExists(LPCWSTR functionName, int numArgs) = 0;
        virtual void ArrayGetAt(const qvalue_t &x, int numIndices,
                                BoundaryAccessMode boundsMode) = 0;
        virtual void ArraySetAt(qvalue_t & x, int numIndices,
                                BoundaryAccessMode boundsMode) = 0;
        virtual void Synchronize() = 0;
		virtual void ParallelDo(const qvalue_t& dims, const qvalue_t& function, int numArgs, qvalue_t &retval) = 0;
		virtual void SerialDo(const qvalue_t& dims, const qvalue_t& function, int numArgs, qvalue_t &retval) = 0;
    };
}

#endif
