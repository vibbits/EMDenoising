//
// quasar_host.cpp : Defines the bridge with the Quasar run-time
// environment and compilation system.
//
#define __QUASAR_NO_EXPORTS__
#include "quasar_host.h"
#include "quasar_ncube.h"

#define UNICODE
#if defined(_WIN32) || defined(_WIN64)
#include <crtdbg.h>
#include <Windows.h>
#include <metahost.h>
#define MS_DOTNET
#endif

// MSVC only
#ifdef _MSC_VER
#pragma comment(lib, "mscoree.lib")

// Import mscorlib.tlb (Microsoft Common Language Runtime Class Library).
#import "mscorlib.tlb" raw_interfaces_only high_property_prefixes("_get", "_put", "_putref") rename("ReportEvent", "InteropServices_ReportEvent")
using namespace mscorlib;
#endif

//#define QUASAR_PATH L"C:\\Users\\Bart\\Git\\Quasar\\bin\\x86\\Debug"

#define RELEASE_SAFE(x)                                                        \
    if (x)                                                                     \
    {                                                                          \
        (x)->Release();                                                        \
        x = NULL;                                                              \
    }

#define DELETE_HANDLE(context, handle)                                         \
    if (handle)                                                                \
    {                                                                          \
        (context)->Delete(handle);                                             \
        handle = NULL;                                                         \
    }

//======================================================
// Begin interface
//======================================================

namespace quasar
{

    static LPCWSTR typeNames[TYPE_COUNT] = {
        L"??",          L"scalar", L"cscalar", L"int",   L"vec",    L"mat",
        L"cube",        L"cvec",   L"cmat",    L"ccube", L"string", L"type",
        L"lambda_expr", L"int8",   L"int16",   L"int64", L"uint8",  L"uint16",
		L"uint32",      L"uint64", NULL,       L"object",L"cube{n}",L"ccube{n}" };

    IMPLEMENT_TYPE(TYPE_SCALAR, scalar, L"scalar");
    IMPLEMENT_TYPE(TYPE_COMPLEXSCALAR, cscalar, L"cscalar");
    IMPLEMENT_TYPE(TYPE_INT, int, L"int");
    IMPLEMENT_TYPE(TYPE_VEC, Vector, L"vec");
    IMPLEMENT_TYPE(TYPE_MAT, Matrix, L"mat");
    IMPLEMENT_TYPE(TYPE_CUBE, Cube, L"cube");
    IMPLEMENT_TYPE(TYPE_CVEC, CVector, L"cvec");
    IMPLEMENT_TYPE(TYPE_CMAT, CMatrix, L"cmat");
    IMPLEMENT_TYPE(TYPE_CCUBE, CCube, L"ccube");
    IMPLEMENT_TYPE(TYPE_STRING, string_t, L"string");
    IMPLEMENT_TYPE(TYPE_INT8, int8_t, L"int8");
    IMPLEMENT_TYPE(TYPE_INT16, int16_t, L"int16");
    IMPLEMENT_TYPE(TYPE_INT64, int64_t, L"int64");
    IMPLEMENT_TYPE(TYPE_UINT8, uint8_t, L"uint8");
    IMPLEMENT_TYPE(TYPE_UINT16, uint16_t, L"uint16");
    IMPLEMENT_TYPE(TYPE_UINT32, uint32_t, L"uint32");
    IMPLEMENT_TYPE(TYPE_UINT64, uint64_t, L"uint64");
    IMPLEMENT_TYPE(TYPE_VOID, qvalue_t, L"??");

	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<4>, L"cube{4}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<5>, L"cube{5}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<6>, L"cube{6}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<7>, L"cube{7}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<8>, L"cube{8}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<9>, L"cube{9}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<10>, L"cube{10}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<11>, L"cube{11}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<12>, L"cube{12}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<13>, L"cube{13}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<14>, L"cube{14}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<15>, L"cube{15}");
	IMPLEMENT_TYPE(TYPE_NCUBE, NCube<16>, L"cube{16}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<4>, L"ccube{4}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<5>, L"ccube{5}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<6>, L"ccube{6}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<7>, L"ccube{7}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<8>, L"ccube{8}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<9>, L"ccube{9}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<10>, L"ccube{10}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<11>, L"ccube{11}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<12>, L"ccube{12}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<13>, L"ccube{13}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<14>, L"ccube{14}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<15>, L"ccube{15}");
	IMPLEMENT_TYPE(TYPE_NCCUBE, NCCube<16>, L"ccube{16}");

    struct QuasarContext;
    class ObjBaseImpl;
    class ComputationEngineImpl;
    class EvaluationStackImpl;
    class MatrixFactoryImpl;
    class TypeEnvironmentImpl;
    class RuntimeReductionEngineImpl;

    // The following struct defines the ABI with Quasar, and should not be
    // used from user code
    struct QuasarContext
    {
        wchar_t deviceName[256];
        int useDoublePrecision;
        wchar_t errorMsg[2048];


        bool (*FunctionExists)(LPCWSTR functionName);
        bool (*FunctionCall)(LPCWSTR functionName, LPCVOID argsIn, int nArgsIn,
                             LPVOID argsOut, int nArgsOut);
        bool (*FunctionCallIndirect)(qhandle lambda, LPCVOID argsIn,
                                     int nArgsIn, LPVOID argsOut, int nArgsOut);
        bool (*Close)();
        qhandle (*ParseType)(LPCWSTR typeName);
        bool (*Delete)(qhandle handle);
        qhandle (*CreateNDMatrix)(qhandle elemType, const int *dims, int num_dims);
        qhandle (*CreateString)(LPCWSTR text, int length);
        qhandle (*CreateObject)(qhandle objType);
        qhandle (*CreateLambda)(qhandle functionTypeHandle,
                                LambdaDelegate lambda);
        void (*AddRef)(qhandle handle);
        bool (*Release)(qhandle handle);
        LockResults (*Lock)(qhandle handle, LockingModes lockingMode,
                            MemResources memResource, void **pData);
        void (*Unlock)(qhandle handle, LockingModes lockingMode,
                       MemResources memResource);
        void (*RunApp)();
        void (*DoEvents)();
        bool (*ReadVariable)(LPCWSTR varname, qvalue_t *value);
        bool (*WriteVariable)(LPCWSTR varname, const qvalue_t *value);
        bool (*GetField)(qhandle obj, LPCWSTR fieldName, qvalue_t *value);
        bool (*SetField)(qhandle obj, LPCWSTR fieldName, const qvalue_t *value);
        qhandle (*CreateType)(LPCWSTR moduleName, LPCWSTR typeName);
        bool (*Type_AddField)(qhandle typeHandle, LPCWSTR fieldName,
                              qhandle fieldType);
        qhandle (*Type_AddParameter)(qhandle typeHandle, LPCWSTR paramName);
        void (*Type_Finalize)(qhandle typeHandle);
        bool (*LoadSourceModule)(LPCWSTR moduleName, LPCWSTR *errorMsg,
                                 LPCVOID reserved);
        bool (*LoadBinaryModule)(LPCWSTR moduleName, LPCWSTR *errorMsg,
                                 LPCVOID reserved);
        bool (*LoadModuleFromSource)(LPCWSTR moduleName, LPCWSTR sourceString,
                                     LPCWSTR *errorMsg, LPCVOID reserved);
        qhandle (*LookupFunction)(LPCWSTR functionSignature);
        qhandle (*GetType)(qhandle handle, DataTypes type);
        qhandle (*LookupMethod)(qhandle typeHandle, LPCWSTR functionSignature);
        bool (*MethodCall)(qhandle methodHandle, qhandle objHandle,
                           DataTypes objType, LPCVOID argsIn, int nArgsIn,
                           LPVOID argsOut, int nArgsOut);
        bool (*EnableProfiling)(ProfilingModes profilingMode,
                                LPCWSTR outputFileName);
        qhandle (*GetComputationEngine)(LPCVOID reserved);
        bool (*ComputationEngine_GetName)(qhandle engine, LPWSTR name,
                                          int maxChars);
        qhandle (*ComputationEngine_GetEvaluationStack)(qhandle engine,
                                                        LPCVOID reserved);
        qhandle (*ComputationEngine_GetMatrixFactory)(qhandle engine);
        qhandle (*ComputationEngine_GetTypeEnvironment)(qhandle engine);
        qhandle (*ComputationEngine_GetRuntimeReductionEngine)(qhandle engine);
        void (*ComputationEngine_Process)(qhandle engine, OperatorTypes op);
        void (*ComputationEngine_ConstructMatrix)(qhandle engine,
                                                  int numRowsOrCols,
                                                  int unusedFlags);
        void (*ComputationEngine_ConstructCellMatrix)(qhandle engine,
                                                      int length,
                                                      int unusedFlags);
        bool (*ComputationEngine_FunctionCall)(qhandle engine,
                                               LPCWSTR functionName,
                                               int numArgs, int unusedFlags);
        bool (*ComputationEngine_FunctionExists)(qhandle engine,
                                                 LPCWSTR functionName,
                                                 int numArgs, int unusedFlags);
        void (*ComputationEngine_ArrayGetAt)(qhandle engine, const qvalue_t *x,
                                             int numIndices,
                                             BoundaryAccessMode boundsMode);
        void (*ComputationEngine_ArraySetAt)(qhandle engine, qvalue_t *x,
                                             int numIndices,
                                             BoundaryAccessMode boundsMode);
        void (*ComputationEngine_Synchronize)(qhandle engine, int unusedFlags);
        void (*Stack_PushMultiple)(qhandle stack, const qvalue_t *x, int cnt);
        void (*Stack_PopMultiple)(qhandle stack, qvalue_t *x, int cnt);
        void (*Stack_Clear)(qhandle stack);
        void (*Stack_PushContext)(qhandle stack);
        void (*Stack_PopContext)(qhandle stack);
        void (*Stack_Process)(qhandle stack, OperatorTypes op);
        void (*Stack_FunctionCall)(qhandle stack, LPCWSTR functionName,
                                   int numArgs, int unusedFlags);
        void (*Stack_ArrayGetAt)(qhandle stack, const qvalue_t *x,
                                 int numIndices, BoundaryAccessMode boundsMode);
        void (*Stack_ArraySetAt)(qhandle stack, qvalue_t *x, int numIndices,
                                 BoundaryAccessMode boundsMode);
        void (*Stack_Evaluate)(qhandle stack, qvalue_t *x);
        int (*Stack_GetCount)(qhandle stack);
        void (*MatrixFactory_New)(qhandle factory, qvalue_t *y,
                                  AllocationFlags flags, LPCVOID elems,
                                  int nDims, const int *dims);
        ScalarTypes (*TypeEnvironment_GetScalarType)(qhandle typeEnv);
        bool (*RuntimeReductionEngine_Add)(qhandle rre, LPWSTR searchExpr,
                                           LambdaDelegate action,
                                           LambdaDelegate whereClause,
                                           int unusedFlags);
        bool (*RuntimeReductionEngine_Remove)(qhandle rre, LPWSTR searchExpr,
                                              int unusedFlags);
		bool (*QueryProperty)(HostProperties prop, int param, 
							  void** dst, size_t nBytes);
		bool (*UnloadModule)(LPCWSTR moduleName);
		void (*ComputationEngine_ParallelDo)(qhandle engine, const qvalue_t *dims, const qvalue_t *function, int numArgs, qvalue_t *retval);
		void (*ComputationEngine_SerialDo)(qhandle engine, const qvalue_t *dims, const qvalue_t *function, int numArgs, qvalue_t *retval);
		void (*GetNDims)(qhandle matrix, int* dims, int* ndims);
    };

    class ObjBaseImpl
    {
      protected:
        int refCount;

      public:
        ObjBaseImpl();
        void AddRef();
        void Release();
    };

    ObjBaseImpl::ObjBaseImpl() { refCount = 1; }

    void ObjBaseImpl::AddRef() { refCount++; }

    void ObjBaseImpl::Release()
    {
        if (--refCount == 0)
            delete this;
    }

    class ComputationEngineImpl : public ObjBaseImpl, public IComputationEngine
    {
        QuasarContext *context;
        EvaluationStackImpl *evaluationStack;
        MatrixFactoryImpl *matrixFactory;
        TypeEnvironmentImpl *typeEnvironment;
        RuntimeReductionEngineImpl *rre;
        qhandle handle;

      public:
        ComputationEngineImpl(QuasarContext *context);
        virtual ~ComputationEngineImpl();
        bool GetName(LPWSTR name, int maxChars);
        IEvaluationStack *GetEvaluationStack();
        IMatrixFactory *GetMatrixFactory();
        ITypeEnvironment *GetTypeEnvironment();
        IRuntimeReductionEngine *GetRuntimeReductionEngine();
        void Process(OperatorTypes op);
        void ConstructMatrix(int numRowsOrCols);
        void ConstructCellMatrix(int length);
        bool FunctionCall(LPCWSTR functionName, int numArgs);
        bool FunctionExists(LPCWSTR functionName, int numArgs);
        void ArrayGetAt(const qvalue_t &x, int numIndices,
                        BoundaryAccessMode boundsMode);
        void ArraySetAt(qvalue_t &x, int numIndices,
                        BoundaryAccessMode boundsMode);
        void Synchronize();
		void ParallelDo(const qvalue_t& dims, const qvalue_t& function, int numArgs, qvalue_t &retval);
		void SerialDo(const qvalue_t& dims, const qvalue_t& function, int numArgs, qvalue_t &retval);
        void AddRef() { ObjBaseImpl::AddRef(); }
        void Release()
        {
            if (--refCount == 0)
                delete this;
        }
        qhandle GetHandle() { return handle; }
    };

    class EvaluationStackImpl : public ObjBaseImpl, public IEvaluationStack
    {
        QuasarContext *context;
        qhandle handle;

      public:
        EvaluationStackImpl(QuasarContext *context,
                            ComputationEngineImpl *engine);
        virtual ~EvaluationStackImpl();
        qvalue_t PopValue();
        void PushValue(const qvalue_t &value);
        void PopMultiple(qvalue_t *vals, int count);
        void PushMultiple(const qvalue_t *vals, int count);
        void Clear();
        void PushContext();
        void PopContext();
        int GetCount();
        void Process(OperatorTypes op);
        void FunctionCall(LPCWSTR functionName, int numArgs);
        void ArrayGetAt(const qvalue_t &x, int numIndices,
                        BoundaryAccessMode boundsMode);
        void ArraySetAt(qvalue_t &x, int numIndices,
                        BoundaryAccessMode boundsMode);
        qvalue_t Evaluate(const qvalue_t &x);
        void AddRef() { ObjBaseImpl::AddRef(); }
        void Release()
        {
            if (--refCount == 0)
                delete this;
        }
    };


    class MatrixFactoryImpl : public ObjBaseImpl, public IMatrixFactory
    {
        QuasarContext *context;
        qhandle handle;

      public:
        MatrixFactoryImpl(QuasarContext *context,
                          ComputationEngineImpl *engine);
        virtual ~MatrixFactoryImpl();
        qvalue_t New(AllocationFlags flags, LPCVOID elems, int nDims,
                     const int *dims);
        void AddRef() { ObjBaseImpl::AddRef(); }
        void Release()
        {
            if (--refCount == 0)
                delete this;
        }
    };

    class TypeEnvironmentImpl : public ObjBaseImpl, public ITypeEnvironment
    {
        QuasarContext *context;
        qhandle handle;

      public:
        TypeEnvironmentImpl(QuasarContext *context,
                            ComputationEngineImpl *engine);
        virtual ~TypeEnvironmentImpl();
        ScalarTypes GetScalarType();
        void AddRef() { ObjBaseImpl::AddRef(); }
        void Release()
        {
            if (--refCount == 0)
                delete this;
        }
    };

    class RuntimeReductionEngineImpl : public ObjBaseImpl,
                                       public IRuntimeReductionEngine
    {
        QuasarContext *context;
        qhandle handle;

      public:
        RuntimeReductionEngineImpl(QuasarContext *context,
                                   ComputationEngineImpl *engine);
        virtual ~RuntimeReductionEngineImpl();
        bool Add(LPWSTR searchExpr, LambdaDelegate action,
                 LambdaDelegate whereClause);
        bool Remove(LPWSTR searchExpr);
        void AddRef() { ObjBaseImpl::AddRef(); }
        void Release()
        {
            if (--refCount == 0)
                delete this;
        }
    };

#ifndef MS_DOTNET
    typedef void ICLRMetaHost;
    typedef void ICLRRuntimeInfo;
    typedef void ICLRRuntimeHost;
#endif

    /*
     * The main implementation for the Quasar host!
     */
    class QuasarHostImpl : public ObjBaseImpl, public IQuasarHost
    {
      private:
        QuasarContext context;
        ICLRMetaHost *pMetaHost;
        ICLRRuntimeInfo *pRuntimeInfo;
        ICLRRuntimeHost *pClrRuntimeHost;
        ComputationEngineImpl *engine;
        qhandle typeHandles[TYPE_COUNT];
        bool isRefCountable[TYPE_COUNT];
        static QuasarHostImpl *instance;

      public:
        QuasarHostImpl(const QuasarContext &context, ICLRMetaHost *pMetaHost,
                       ICLRRuntimeInfo *pRuntimeInfo,
                       ICLRRuntimeHost *pClrRuntimeHost);
        bool FunctionExists(LPCWSTR functionName);
        bool FunctionCall(LPCWSTR functionName, const qvalue_t *inArgs,
                          int nInArgs, qvalue_t *outArgs, int nOutArgs);
        bool FunctionCall(const qvalue_t &function, const qvalue_t *inArgs,
                          int nInArgs, qvalue_t *outArgs, int nOutArgs);
        qvalue_t CreateVector(qhandle elemType, int numel);
        qvalue_t CreateMatrix(qhandle elemType, int dim1, int dim2);
        qvalue_t CreateCube(qhandle elemType, int dim1, int dim2, int dim3);
		qvalue_t CreateNCube(qhandle elemType, int ndims, const int* dims);
        qvalue_t CreateString(LPCWSTR text, int length);
        qvalue_t CreateTypedObject(qhandle objType);
        qvalue_t CreateUntypedObject(qhandle objType);
        qvalue_t CreateLambda(qhandle functionTypeHandle,
                              LambdaDelegate lambda);
        qvalue_t LookupFunction(LPCWSTR functionSignature);
        qvalue_t LookupType(LPCWSTR quasarTypeName);
        qvalue_t LookupMethod(const qvalue_t &type, LPCWSTR functionSignature);
        bool MethodCall(const qvalue_t &function, const qvalue_t &target,
                        const qvalue_t *inArgs, int nInArgs, qvalue_t *outArgs,
                        int nOutArgs);
        void AddRef(qvalue_t &obj);
        void ReleaseRef(qvalue_t &obj);
        void DeleteValue(qvalue_t &obj);
		void GetNDims(const qvalue_t &obj, int *ndims, int *dims);
        LockResults Lock(const qvalue_t &obj, DataTypes elemType,
                         LockingModes lockingMode, MemResources memResource,
                         void **pData);
        void Unlock(const qvalue_t &obj, LockingModes lockingMode,
                    MemResources memResource);
        qhandle GetPrimitiveTypeHandle(DataTypes type);
        virtual ~QuasarHostImpl();
        void RunApp() { context.RunApp(); }
        void DoEvents() { context.DoEvents(); }
        bool ReadVariable(LPCWSTR varName, qvalue_t &val);
        bool WriteVariable(LPCWSTR varName, const qvalue_t &val);
        bool GetField(const qvalue_t &obj, LPCWSTR fieldName, qvalue_t &val);
        bool SetField(const qvalue_t &obj, LPCWSTR fieldName,
                      const qvalue_t &val);
        bool LoadSourceModule(LPCWSTR moduleName, LPCWSTR *errorMsg);
        bool LoadBinaryModule(LPCWSTR binaryModuleName, LPCWSTR *errorMsg);
        bool LoadModuleFromSource(LPCWSTR moduleName, LPCWSTR sourceString,
                                  LPCWSTR *errorMsg);
		bool UnloadModule(LPCWSTR moduleName);
        LPCWSTR GetLastErrorMsg() { return context.errorMsg; }
        qvalue_t GetType(const qvalue_t &obj);
        qvalue_t CreateType(LPCWSTR moduleName, LPCWSTR typeName);
        bool AddField(qhandle objType, LPCWSTR fieldName, qhandle fieldType);
        qvalue_t AddParameter(qhandle objType, LPCWSTR paramName);
        void FinalizeType(qhandle typeHandle);
        bool EnableProfiling(ProfilingModes profilingMode,
                             LPCWSTR outputFileName);
		bool QueryProperty(HostProperties prop, int param, void** dst, size_t nBytes);
        IEvaluationStack *CreateStack();
        IComputationEngine *GetComputationEngine();
        void AddRef() { ObjBaseImpl::AddRef(); }
        void Release()
        {
            if (--refCount == 0)
                delete this;
        }
        static QuasarHostImpl *GetInstance() { return instance; }
    };
}

//======================================================
// Begin implementation
//======================================================

namespace quasar
{


    ComputationEngineImpl::ComputationEngineImpl(QuasarContext *context)
    {
        this->context = context;
        this->handle = context->GetComputationEngine(NULL);
        this->evaluationStack = NULL;
        this->rre = NULL;
        this->matrixFactory = NULL;
        this->typeEnvironment = NULL;
    }

    bool ComputationEngineImpl::GetName(LPWSTR name, int maxChars)
    {
        return context->ComputationEngine_GetName(handle, name, maxChars);
    }

    void ComputationEngineImpl::Process(OperatorTypes op)
    {
        context->ComputationEngine_Process(handle, op);
    }

    void ComputationEngineImpl::ConstructMatrix(int numRowsOrCols)
    {
        context->ComputationEngine_ConstructMatrix(handle, numRowsOrCols, 0);
    }

    void ComputationEngineImpl::ConstructCellMatrix(int length)
    {
        context->ComputationEngine_ConstructCellMatrix(handle, length, 0);
    }

    bool ComputationEngineImpl::FunctionCall(LPCWSTR functionName, int numArgs)
    {
        return context->ComputationEngine_FunctionCall(handle, functionName,
                                                       numArgs, 0);
    }

    bool ComputationEngineImpl::FunctionExists(LPCWSTR functionName,
                                               int numArgs)
    {
        return context->ComputationEngine_FunctionExists(handle, functionName,
                                                         numArgs, 0);
    }

    void ComputationEngineImpl::ArrayGetAt(const qvalue_t &x, int numIndices,
                                           BoundaryAccessMode boundsMode)
    {
        context->ComputationEngine_ArrayGetAt(handle, &x, numIndices,
                                              boundsMode);
    }

    void ComputationEngineImpl::ArraySetAt(qvalue_t &x, int numIndices,
                                           BoundaryAccessMode boundsMode)
    {
        context->ComputationEngine_ArraySetAt(handle, &x, numIndices,
                                              boundsMode);
    }

    void ComputationEngineImpl::Synchronize()
    {
        context->ComputationEngine_Synchronize(handle, 0);
    }

	void ComputationEngineImpl::ParallelDo(const qvalue_t& dims, const qvalue_t& function, int numArgs, qvalue_t &retval)
	{
		context->ComputationEngine_ParallelDo(handle, &dims, &function, numArgs, &retval);
	}

	void ComputationEngineImpl::SerialDo(const qvalue_t& dims, const qvalue_t& function, int numArgs, qvalue_t &retval)
	{
		context->ComputationEngine_SerialDo(handle, &dims, &function, numArgs, &retval);
	}


    IEvaluationStack *ComputationEngineImpl::GetEvaluationStack()
    {
        if (evaluationStack == NULL)
            evaluationStack = new EvaluationStackImpl(context, this);
        return evaluationStack;
    }

    IMatrixFactory *ComputationEngineImpl::GetMatrixFactory()
    {
        if (matrixFactory == NULL)
            matrixFactory = new MatrixFactoryImpl(context, this);
        return matrixFactory;
    }

    ITypeEnvironment *ComputationEngineImpl::GetTypeEnvironment()
    {
        if (typeEnvironment == NULL)
            typeEnvironment = new TypeEnvironmentImpl(context, this);
        return typeEnvironment;
    }

    IRuntimeReductionEngine *ComputationEngineImpl::GetRuntimeReductionEngine()
    {
        if (rre == NULL)
            rre = new RuntimeReductionEngineImpl(context, this);
        return rre;
    }

    ComputationEngineImpl::~ComputationEngineImpl()
    {
        RELEASE_SAFE(evaluationStack);
        RELEASE_SAFE(matrixFactory);
        RELEASE_SAFE(typeEnvironment);
        RELEASE_SAFE(rre);
    }


    EvaluationStackImpl::EvaluationStackImpl(QuasarContext *context,
                                             ComputationEngineImpl *engine)
    {
        this->context = context;
        this->handle = context->ComputationEngine_GetEvaluationStack(
            engine->GetHandle(), NULL);
    }

    EvaluationStackImpl::~EvaluationStackImpl()
    {
        DELETE_HANDLE(context, handle);
    }

    qvalue_t EvaluationStackImpl::PopValue()
    {
        qvalue_t x;
        context->Stack_PopMultiple(handle, &x, 1);
        return x;
    }

    void EvaluationStackImpl::PushValue(const qvalue_t &value)
    {
        context->Stack_PushMultiple(handle, &value, 1);
    }

    void EvaluationStackImpl::PopMultiple(qvalue_t *vals, int count)
    {
        context->Stack_PopMultiple(handle, vals, count);
    }

    void EvaluationStackImpl::PushMultiple(const qvalue_t *vals, int count)
    {
        context->Stack_PushMultiple(handle, vals, count);
    }

    void EvaluationStackImpl::Clear() { context->Stack_Clear(handle); }

    void EvaluationStackImpl::PushContext()
    {
        context->Stack_PushContext(handle);
    }

    void EvaluationStackImpl::PopContext()
    {
        context->Stack_PopContext(handle);
    }

    void EvaluationStackImpl::Process(OperatorTypes op)
    {
        context->Stack_Process(handle, op);
    }

    void EvaluationStackImpl::FunctionCall(LPCWSTR functionName, int numArgs)
    {
        context->Stack_FunctionCall(handle, functionName, numArgs, 0);
    }

    void EvaluationStackImpl::ArrayGetAt(const qvalue_t &x, int numIndices,
                                         BoundaryAccessMode boundsMode)
    {
        context->Stack_ArrayGetAt(handle, &x, numIndices, boundsMode);
    }

    void EvaluationStackImpl::ArraySetAt(qvalue_t &x, int numIndices,
                                         BoundaryAccessMode boundsMode)
    {
        context->Stack_ArraySetAt(handle, &x, numIndices, boundsMode);
    }

    qvalue_t EvaluationStackImpl::Evaluate(const qvalue_t &x)
    {
        qvalue_t a = x;
        context->Stack_Evaluate(handle, &a);
        return a;
    }

    int EvaluationStackImpl::GetCount()
    {
        return context->Stack_GetCount(handle);
    }

    MatrixFactoryImpl::MatrixFactoryImpl(QuasarContext *context,
                                         ComputationEngineImpl *engine)
    {
        this->context = context;
        this->handle =
            context->ComputationEngine_GetMatrixFactory(engine->GetHandle());
    }

    MatrixFactoryImpl::~MatrixFactoryImpl() { DELETE_HANDLE(context, handle); }

    qvalue_t MatrixFactoryImpl::New(AllocationFlags flags, LPCVOID elems,
                                    int nDims, const int *dims)
    {
        qvalue_t t;
        context->MatrixFactory_New(handle, &t, flags, elems, nDims, dims);
        return t;
    }

    TypeEnvironmentImpl::TypeEnvironmentImpl(QuasarContext *context,
                                             ComputationEngineImpl *engine)
    {
        this->context = context;
        this->handle =
            context->ComputationEngine_GetTypeEnvironment(engine->GetHandle());
    }

    TypeEnvironmentImpl::~TypeEnvironmentImpl()
    {
        DELETE_HANDLE(context, handle);
    }

    ScalarTypes TypeEnvironmentImpl::GetScalarType()
    {
        return context->TypeEnvironment_GetScalarType(handle);
    }

    RuntimeReductionEngineImpl::RuntimeReductionEngineImpl(
        QuasarContext *context, ComputationEngineImpl *engine)
    {
        this->context = context;
        this->handle =
            context->ComputationEngine_GetRuntimeReductionEngine(engine);
    }

    RuntimeReductionEngineImpl::~RuntimeReductionEngineImpl()
    {
        DELETE_HANDLE(context, handle);
    }

    bool RuntimeReductionEngineImpl::Add(LPWSTR searchExpr,
                                         LambdaDelegate action,
                                         LambdaDelegate whereClause)
    {
        return context->RuntimeReductionEngine_Add(handle, searchExpr, action,
                                                   whereClause, 0);
    }

    bool RuntimeReductionEngineImpl::Remove(LPWSTR searchExpr)
    {
        return context->RuntimeReductionEngine_Remove(handle, searchExpr, 0);
    }

    QuasarHostImpl *QuasarHostImpl::instance;

    QuasarHostImpl::QuasarHostImpl(const QuasarContext &context,
                                   ICLRMetaHost *pMetaHost,
                                   ICLRRuntimeInfo *pRuntimeInfo,
                                   ICLRRuntimeHost *pClrRuntimeHost)
    {
        QuasarHostImpl::instance = this;
        this->context = context;
        this->pMetaHost = pMetaHost;
        this->pRuntimeInfo = pRuntimeInfo;
        this->pClrRuntimeHost = pClrRuntimeHost;

        for (int i = 0; i < TYPE_COUNT; i++)
        {
            typeHandles[i] =
                typeNames[i] ? context.ParseType(typeNames[i]) : NULL;
            isRefCountable[i] = false;
        }
        isRefCountable[TYPE_VEC] = true;
        isRefCountable[TYPE_MAT] = true;
        isRefCountable[TYPE_CUBE] = true;
        isRefCountable[TYPE_CVEC] = true;
        isRefCountable[TYPE_CMAT] = true;
        isRefCountable[TYPE_CCUBE] = true;
		isRefCountable[TYPE_NCUBE] = true;
		isRefCountable[TYPE_NCCUBE] = true;
        isRefCountable[TYPE_TYPEDOBJECT] = true;
        isRefCountable[TYPE_UNTYPEDOBJECT] = true;

        this->engine = new ComputationEngineImpl(&this->context);
    }

    QuasarHostImpl::~QuasarHostImpl()
    {
        for (int i = 0; i < TYPE_COUNT; i++)
            DELETE_HANDLE(&context, typeHandles[i]);
        RELEASE_SAFE(engine);
        if (context.Close)
        {
            context.Close();
            context.Close = NULL;
        }
#ifdef MS_DOTNET
        RELEASE_SAFE(pMetaHost);
        RELEASE_SAFE(pRuntimeInfo);
        RELEASE_SAFE(pClrRuntimeHost);
#endif
        QuasarHostImpl::instance = NULL;
    }

    bool QuasarHostImpl::FunctionExists(LPCWSTR functionName)
    {
        return context.FunctionExists(functionName);
    }

    bool QuasarHostImpl::FunctionCall(LPCWSTR functionName,
                                      const qvalue_t *inArgs, int nInArgs,
                                      qvalue_t *outArgs, int nOutArgs)
    {
        return context.FunctionCall(functionName, inArgs, nInArgs, outArgs,
                                    nOutArgs);
    }

    bool QuasarHostImpl::FunctionCall(const qvalue_t &value,
                                      const qvalue_t *inArgs, int nInArgs,
                                      qvalue_t *outArgs, int nOutArgs)
    {
        _ASSERT(value.type == TYPE_LAMBDAEXPR);
        return context.FunctionCallIndirect(value.private_obj, inArgs, nInArgs,
                                            outArgs, nOutArgs);
    }


    qvalue_t QuasarHostImpl::CreateVector(qhandle elemType, int numel)
    {
        int dims[16] = {1,numel};

        qvalue_t t;
        t.type = TYPE_VEC;
        t.ptrVal = NULL;
        t.dim1 = 1;
        t.dim2 = numel;
        t.dim3 = 1;
        t.private_obj = context.CreateNDMatrix(elemType, dims, 2);
        return t;
    }

    qvalue_t QuasarHostImpl::CreateMatrix(qhandle elemType, int dim1, int dim2)
    {
        int dims[16] = {dim1, dim2};

        qvalue_t t;
        t.type = TYPE_MAT;
        t.ptrVal = NULL;
        t.dim1 = dim1;
        t.dim2 = dim2;
        t.dim3 = 1;
        t.private_obj = context.CreateNDMatrix(elemType, dims, 2);
        return t;
    }

    qvalue_t QuasarHostImpl::CreateCube(qhandle elemType, int dim1, int dim2,
                                        int dim3)
    {
        int dims[16] = {dim1, dim2, dim3};

        qvalue_t t;
        t.type = TYPE_CUBE;
        t.ptrVal = NULL;
        t.dim1 = dim1;
        t.dim2 = dim2;
        t.dim3 = dim3;
        t.private_obj = context.CreateNDMatrix(elemType, dims, 3);
        return t;
    }

	qvalue_t QuasarHostImpl::CreateNCube(qhandle elemType, int ndims, const int* dims)
	{
		_ASSERT(ndims >= 1);
		if (ndims == 1)
			return CreateVector(elemType, dims[0]);
		qvalue_t t;
        t.type = TYPE_NCUBE;
        t.ptrVal = NULL;
        t.dim1 = dims[0];
        t.dim2 = ndims > 1 ? dims[1] : 1;
        t.dim3 = ndims > 2 ? dims[2] : 1;
        t.private_obj = context.CreateNDMatrix(elemType, dims, ndims);
        return t;
	}


    qvalue_t QuasarHostImpl::CreateString(LPCWSTR text, int length)
    {
        // string length not specified
        if (length < 0)
            length = wcslen(text);
        qvalue_t t;
        t.type = TYPE_STRING;
        t.private_obj = context.CreateString(text, length);
        return t;
    }

    qvalue_t QuasarHostImpl::CreateTypedObject(qhandle objType)
    {
        qvalue_t t;
        t.type = TYPE_TYPEDOBJECT;
        t.private_obj = context.CreateObject(objType);
        return t;
    }

    qvalue_t QuasarHostImpl::CreateUntypedObject(qhandle objType)
    {
        qvalue_t t;
        t.type = TYPE_UNTYPEDOBJECT;
        t.private_obj = context.CreateObject(objType);
        return t;
    }

    qvalue_t QuasarHostImpl::CreateLambda(qhandle functionTypeHandle,
                                          LambdaDelegate lambda)
    {
        qvalue_t t;
        t.type = TYPE_LAMBDAEXPR;
        t.private_obj = context.CreateLambda(functionTypeHandle, lambda);
        return t;
    }

    qvalue_t QuasarHostImpl::LookupFunction(LPCWSTR functionName)
    {
        qvalue_t t;
        t.type = TYPE_LAMBDAEXPR;
        t.private_obj = context.LookupFunction(functionName);
        return t;
    }

    qvalue_t QuasarHostImpl::LookupType(LPCWSTR quasarTypeName)
    {
        qvalue_t t;
        t.type = TYPE_TYPEINFO;
        t.private_obj = context.ParseType(quasarTypeName);
        return t;
    }

    qvalue_t QuasarHostImpl::LookupMethod(const qvalue_t &type,
                                          LPCWSTR functionSignature)
    {
        _ASSERT(type.type == TYPE_TYPEINFO);
        qvalue_t t;
        t.type = TYPE_METHOD;
        t.private_obj =
            context.LookupMethod(type.private_obj, functionSignature);
        return t;
    }

    bool QuasarHostImpl::MethodCall(const qvalue_t &function,
                                    const qvalue_t &target,
                                    const qvalue_t *inArgs, int nInArgs,
                                    qvalue_t *outArgs, int nOutArgs)
    {
        _ASSERT(function.type == TYPE_METHOD &&
                (target.type == TYPE_TYPEDOBJECT ||
                 target.type == TYPE_UNTYPEDOBJECT));
        return context.MethodCall(function.private_obj, target.private_obj,
                                  target.type, inArgs, nInArgs, outArgs,
                                  nOutArgs);
    }

    qvalue_t QuasarHostImpl::GetType(const qvalue_t &obj)
    {
        qvalue_t t;
        t.type = TYPE_TYPEINFO;
        t.private_obj = context.GetType(obj.private_obj, obj.type);
        return t;
    }

    qvalue_t QuasarHostImpl::CreateType(LPCWSTR moduleName, LPCWSTR typeName)
    {
        qvalue_t t;
        t.type = TYPE_TYPEINFO;
        t.private_obj = context.CreateType(moduleName, typeName);
        return t;
    }

    bool QuasarHostImpl::AddField(qhandle objType, LPCWSTR fieldName,
                                  qhandle fieldType)
    {
        return context.Type_AddField(objType, fieldName, fieldType);
    }

    qvalue_t QuasarHostImpl::AddParameter(qhandle objType, LPCWSTR paramName)
    {
        qvalue_t t;
        t.type = TYPE_TYPEINFO;
        t.private_obj = context.Type_AddParameter(objType, paramName);
        return t;
    }

    void QuasarHostImpl::FinalizeType(qhandle typeHandle)
    {
        context.Type_Finalize(typeHandle);
    }


    void QuasarHostImpl::AddRef(qvalue_t &obj)
    {
        if (isRefCountable[obj.type])
        {
            context.AddRef(obj.private_obj);
        }
    }

    void QuasarHostImpl::ReleaseRef(qvalue_t &obj)
    {
        if (isRefCountable[obj.type])
        {
            if (context.Release(obj.private_obj))
            {
                // The object has been released!
                obj.private_obj = NULL;
            }
        }
    }

    void QuasarHostImpl::DeleteValue(qvalue_t &obj)
    {
        if (obj.private_obj == NULL)
            return; // Already deleted

        if (isRefCountable[obj.type])
        {
            if (context.Release(obj.private_obj))
                obj.private_obj = NULL;
        }
        else
            switch (obj.type)
            {
            case TYPE_STRING:
                if (context.Delete(obj.private_obj))
                    obj.private_obj = NULL;
                break;
            }
    }

	void QuasarHostImpl::GetNDims(const qvalue_t &obj, int *ndims, int *dims)
	{
		if (isRefCountable[obj.type])
			context.GetNDims(obj.private_obj, dims, ndims);
		else
			*ndims = 0;
	}

    IEvaluationStack *QuasarHostImpl::CreateStack()
    {
        // TODO
        return NULL;
    }

	bool QuasarHostImpl::QueryProperty(HostProperties prop, int param, void** dst, size_t nBytes)
	{
		return context.QueryProperty(prop, param, dst, nBytes);
	}

    qhandle QuasarHostImpl::GetPrimitiveTypeHandle(DataTypes type)
    {
        return typeHandles[type];
    }

    LockResults QuasarHostImpl::Lock(const qvalue_t &obj, DataTypes elemType,
                                     LockingModes lockingMode,
                                     MemResources memResource, void **pData)
    {
        return context.Lock(obj.private_obj, lockingMode, memResource, pData);
    }

    void QuasarHostImpl::Unlock(const qvalue_t &obj, LockingModes lockingMode,
                                MemResources memResource)
    {
        context.Unlock(obj.private_obj, lockingMode, memResource);
    }

    bool QuasarHostImpl::ReadVariable(LPCWSTR varName, qvalue_t &val)
    {
        return context.ReadVariable(varName, &val);
    }

    bool QuasarHostImpl::WriteVariable(LPCWSTR varName, const qvalue_t &val)
    {
        return context.WriteVariable(varName, &val);
    }

    bool QuasarHostImpl::GetField(const qvalue_t &obj, LPCWSTR fieldName,
                                  qvalue_t &val)
    {
        _ASSERT(obj.type == TYPE_UNTYPEDOBJECT || obj.type == TYPE_TYPEDOBJECT);
        return context.GetField(obj.private_obj, fieldName, &val);
    }

    bool QuasarHostImpl::SetField(const qvalue_t &obj, LPCWSTR fieldName,
                                  const qvalue_t &val)
    {
        _ASSERT(obj.type == TYPE_UNTYPEDOBJECT || obj.type == TYPE_TYPEDOBJECT);
        return context.SetField(obj.private_obj, fieldName, &val);
    }

    bool QuasarHostImpl::LoadSourceModule(LPCWSTR sourceModuleFileName,
                                          LPCWSTR *errorMsg)
    {
        return context.LoadSourceModule(sourceModuleFileName, errorMsg, NULL);
    }

    bool QuasarHostImpl::LoadBinaryModule(LPCWSTR binaryModuleName,
                                          LPCWSTR *errorMsg)
    {
        return context.LoadBinaryModule(binaryModuleName, errorMsg, NULL);
    }

    bool QuasarHostImpl::LoadModuleFromSource(LPCWSTR moduleName,
                                              LPCWSTR sourceString,
                                              LPCWSTR *errorMsg)
    {
        return context.LoadModuleFromSource(moduleName, sourceString, errorMsg,
                                            NULL);
    }

	bool QuasarHostImpl::UnloadModule(LPCWSTR moduleName)
	{
		return context.UnloadModule(moduleName);
	}

    bool QuasarHostImpl::EnableProfiling(ProfilingModes profilingMode,
                                         LPCWSTR outputFileName)
    {
        return context.EnableProfiling(profilingMode, outputFileName);
    }

    IComputationEngine *QuasarHostImpl::GetComputationEngine()
    {
        return engine;
    }


//=============================================================================
//=============================================================================

#ifdef MS_DOTNET

    IQuasarHost *IQuasarHost::Create(LPCWSTR deviceName, bool loadCompiler)
    {
        HRESULT hr;

        PCWSTR pszVersion =
            L"v4.0.30319"; // The version of the .NET runtime to load
        PCWSTR pszAssemblyName = L"mscorlib";
        PCWSTR pszTypeName = loadCompiler ? L"Quasar.NativeInterfaceExt" : L"Quasar.NativeInterface";
        PCWSTR pszMethodName = L"InitHost";
        PCWSTR pszArgument = L"";
        wchar_t pwzAssemblyPath[MAX_PATH];
        size_t returnSize;

        // Read the Quasar directory from the environment variables
        _wgetenv_s(&returnSize, pwzAssemblyPath, L"QUASAR_PATH");
		if (loadCompiler)
			lstrcat(pwzAssemblyPath, L"\\Quasar.exe");
		else
			lstrcat(pwzAssemblyPath, L"\\Quasar.Runtime.dll");

        ICLRMetaHost *pMetaHost = NULL;
        ICLRRuntimeInfo *pRuntimeInfo = NULL;

        // Instance already exists
        if (QuasarHostImpl::GetInstance() != NULL)
        {
            wprintf(L"Cannot create two Quasar host interfaces. Use "
                    L"IQuasarHost.GetInstance() "
                    L"to obtain a pointer to the current instance.\n");
            return NULL;
        }

        // ICorRuntimeHost and ICLRRuntimeHost are the two CLR hosting
        // interfaces
        // supported by CLR 4.0. Here we demo the ICLRRuntimeHost interface that
        // was provided in .NET v2.0 to support CLR 2.0 new features.
        // ICLRRuntimeHost does not support loading the .NET v1.x runtimes.
        ICLRRuntimeHost *pClrRuntimeHost = NULL;
        IUnknownPtr spAppDomainThunk = NULL;
        _AppDomainPtr spDefaultAppDomain = NULL;

        // The .NET assembly to load.
        bstr_t bstrAssemblyName(pszAssemblyName);
        _AssemblyPtr spAssembly = NULL;

        //
        // Load and start the .NET runtime.
        //
        wprintf(L"Load and start the .NET runtime %s \n", pszVersion);

        hr = CLRCreateInstance(CLSID_CLRMetaHost, IID_PPV_ARGS(&pMetaHost));
        if (FAILED(hr))
        {
            wprintf(L"CLRCreateInstance failed w/hr 0x%08lx\n", hr);
            goto Cleanup;
        }

        // Get the ICLRRuntimeInfo corresponding to a particular CLR version. It
        // supersedes CorBindToRuntimeEx with STARTUP_LOADER_SAFEMODE.
        hr = pMetaHost->GetRuntime(pszVersion, IID_PPV_ARGS(&pRuntimeInfo));
        if (FAILED(hr))
        {
            wprintf(L"ICLRMetaHost::GetRuntime failed w/hr 0x%08lx\n", hr);
            goto Cleanup;
        }

        // Check if the specified runtime can be loaded into the process. This
        // method will take into account other runtimes that may already be
        // loaded into the process and set pbLoadable to TRUE if this runtime
        // can be loaded in an in-process side-by-side fashion.
        BOOL fLoadable;
        hr = pRuntimeInfo->IsLoadable(&fLoadable);
        if (FAILED(hr))
        {
            wprintf(L"ICLRRuntimeInfo::IsLoadable failed w/hr 0x%08lx\n", hr);
            goto Cleanup;
        }

        if (!fLoadable)
        {
            wprintf(L".NET runtime %s cannot be loaded\n", pszVersion);
            goto Cleanup;
        }

        // Load the CLR into the current process and return a runtime interface
        // pointer. ICorRuntimeHost and ICLRRuntimeHost are the two CLR hosting
        // interfaces supported by CLR 4.0. Here we demo the ICLRRuntimeHost
        // interface that was provided in .NET v2.0 to support CLR 2.0 new
        // features. ICLRRuntimeHost does not support loading the .NET v1.x
        // runtimes.
        hr = pRuntimeInfo->GetInterface(CLSID_CLRRuntimeHost,
                                        IID_PPV_ARGS(&pClrRuntimeHost));
        if (FAILED(hr))
        {
            wprintf(L"ICLRRuntimeInfo::GetInterface failed w/hr 0x%08lx\n", hr);
            goto Cleanup;
        }

        // Start the CLR.
        hr = pClrRuntimeHost->Start();
        if (FAILED(hr))
        {
            wprintf(L"CLR failed to start w/hr 0x%08lx\n", hr);
            goto Cleanup;
        }

        HANDLE hMappedFileObject = NULL;
        LPVOID lpvSharedMem = NULL;

        hMappedFileObject =
            CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0,
                              sizeof(QuasarContext), TEXT("quasar_init"));

        if (hMappedFileObject == NULL)
        {
            wprintf(L"Failed to create file mapping\n");
            goto Cleanup;
        }

        lpvSharedMem =
            MapViewOfFile(hMappedFileObject, FILE_MAP_READ | FILE_MAP_WRITE, 0,
                          0, sizeof(QuasarContext));
        if (lpvSharedMem == NULL)
        {
            wprintf(L"Could not map view of file\n");
            goto Cleanup;
        }

        QuasarContext *params = (QuasarContext *)lpvSharedMem;
        memset(params, 0, sizeof(QuasarContext));
        swprintf_s(params->deviceName, L"%s", deviceName);
#ifdef DBL_SCALAR
        params->useDoublePrecision = true;
#else
        params->useDoublePrecision = false;
#endif
        UnmapViewOfFile(lpvSharedMem);

        DWORD returnValue;
        hr = pClrRuntimeHost->ExecuteInDefaultAppDomain(
            pwzAssemblyPath, pszTypeName, pszMethodName, pszArgument,
            &returnValue);
        if (FAILED(hr))
        {
            wprintf(L"Failed to init Quasar w/hr 0x%08lx\n", hr);
            goto Cleanup;
        }

        // Read back the parameters
        lpvSharedMem =
            MapViewOfFile(hMappedFileObject, FILE_MAP_READ | FILE_MAP_WRITE, 0,
                          0, sizeof(QuasarContext));
        params = (QuasarContext *)lpvSharedMem;

        if (returnValue > 0)
        {
            wprintf(L"Could not load module: %s\n", params->errorMsg);
            goto Cleanup;
        }

        QuasarHostImpl *hostImpl = new QuasarHostImpl(
            *params, pMetaHost, pRuntimeInfo, pClrRuntimeHost);
        UnmapViewOfFile(lpvSharedMem);
        CloseHandle(hMappedFileObject);

        return hostImpl;

    Cleanup:
        RELEASE_SAFE(pMetaHost);
        RELEASE_SAFE(pRuntimeInfo);

        if (pClrRuntimeHost)
        {
// Please note that after a call to Stop, the CLR cannot be
// reinitialized into the same process. This step is usually not
// necessary. You can leave the .NET runtime loaded in your process.
// wprintf(L"Stop the .NET runtime\n");
#if defined(UNLOAD_CLR_ON_CLEANUP)
            pClrRuntimeHost->Stop();
#endif
            RELEASE_SAFE(pClrRuntimeHost);
        }

        return NULL;
    }

#endif

    // Function called from initialization code in Quasar user modules (via the
    // module_init function)
    IQuasarHost *IQuasarHost::Connect(LPCVOID hostprivParams,
                                      LPCVOID moduleDetails, int flags)
    {
        // printf("IQuasarHost::Connect sizeof(qvalue_t)=%d\n",
        // sizeof(qvalue_t));

        // Instance already exists
        if (QuasarHostImpl::GetInstance() != NULL)
        {
            wprintf(L"Cannot create two Quasar host interfaces. Use "
                    L"IQuasarHost.GetInstance() "
                    L"to obtain a pointer to the current instance.\n");
            return NULL;
        }

        return new QuasarHostImpl(*(QuasarContext *)hostprivParams, NULL, NULL,
                                  NULL);
    }

    IQuasarHost *IQuasarHost::GetInstance(void)
    {
        return QuasarHostImpl::GetInstance();
    }

} // namespace quasar
