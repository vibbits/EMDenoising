// Quasar as a domain-specific sublanguage for C++
#ifndef __QUASAR_DSL_H__
#define __QUASAR_DSL_H__

#include "quasar_host.h"

namespace quasar
{
    // Smart reference pointer for IRefCountable classes (i.e. classes
    // that implement an AddRef() and a Release() method).
    template <typename T> class ref // where T : IRefCountable
    {
      protected:
        IRefCountable *obj;

      public:
        ref(IRefCountable *obj) : obj(obj) {}
        ref(const ref &other) : obj(other.obj)
        {
            if (obj)
                obj->AddRef();
        }
        ref() : obj(NULL) {}
        virtual ~ref()
        {
            if (obj != NULL)
            {
                obj->Release();
                obj = NULL;
            }
        }
        T *operator->() { return (T *)obj; }
        const T *operator->() const { return (const T *)obj; }
        bool operator==(T *other) const { return obj == other; }
        bool operator!=(T *other) const { return obj != other; }
        ref &operator=(const ref &x)
        {
            if (obj != NULL)
            {
                obj->Release();
                obj = NULL;
            }
            obj = x.obj;
            obj->AddRef();
            return *this;
        }
        operator const T *() const { return (const T *)obj; }
        operator T *() { return (T *)obj; }
    };

	template<typename>
	struct dereference;

	template<typename T>
	struct dereference<T*>
	{
		typedef T type;
	};

	// Provides automatic locking of objects
	template <typename T>
	class auto_lock : public T
	{
	private:
		qvalue_t obj;
		LockingModes lockingMode;
		MemResources memResource;

		auto_lock(T t, qvalue_t obj, LockingModes lockingMode = LOCK_READWRITE,
							    MemResources memResource = MEMRESOURCE_CPU)
		{
			(T &)*this = t;
			this->lockingMode = lockingMode;
			this->memResource = memResource;
			this->obj = obj;
		}
	public:
		template <typename R>
		static auto_lock lock_vector(qvalue_t obj, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
			return auto_lock(IQuasarHost::GetInstance()->LockVector<R>(obj, lockingMode, memResource), obj, lockingMode, memResource);
		}
		template <typename R>
		static auto_lock lock_matrix(qvalue_t obj, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
			return auto_lock(IQuasarHost::GetInstance()->LockMatrix<R>(obj, lockingMode, memResource), obj, lockingMode, memResource);
		}
		template <typename R>
		static auto_lock lock_cube(qvalue_t obj, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
			return auto_lock(IQuasarHost::GetInstance()->LockCube<R>(obj, lockingMode, memResource), obj, lockingMode, memResource);
		}
		template <typename R, int N>
		static auto_lock lock_ncube(qvalue_t obj, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
			return auto_lock(IQuasarHost::GetInstance()->LockNCube<R, N>(obj, lockingMode, memResource), obj, lockingMode, memResource);
		}
		static auto_lock lock_object(qvalue_t obj, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
			return auto_lock(IQuasarHost::GetInstance()->LockObject<dereference<T>::type>(obj, lockingMode, memResource), obj, lockingMode, memResource);
		}
		~auto_lock()
		{
			IQuasarHost::GetInstance()->Unlock(obj, lockingMode, memResource);
		}
	};

    // Object-oriented wrapper for the raw qvalue_t data type
    class QValue
    {
      protected:
        qvalue_t value;

      public:
        QValue()
        {
            value.type = TYPE_VOID;
            value.private_obj = NULL;
        }
        QValue(float scalarVal) { value = qvalue_t::fromScalar(scalarVal); }
		QValue(double scalarVal) { value = qvalue_t::fromScalar(scalarVal); }
        QValue(cscalar complexVal)
        {
            value = qvalue_t::fromCScalar(complexVal);
        }
		QValue(int intVal) { value = qvalue_t::fromScalar(intVal); }

        template <int N> class Elem
        {
            qvalue_t mtx;
            qvalue_t index[N];
            BoundaryAccessMode boundaryAccessMode;

          public:
            Elem(const qvalue_t &value, qvalue_t index[N],
                 BoundaryAccessMode boundaryAccessMode)
            {
                this->mtx = value;
                this->boundaryAccessMode = boundaryAccessMode;
                for (int i = 0; i < N; i++)
                    this->index[i] = index[i];
                IQuasarHost::GetInstance()->AddRef(mtx);
            }
            ~Elem()
            {
                IQuasarHost::GetInstance()->ReleaseRef(mtx);
                for (int i = 0; i < N; i++)
                    IQuasarHost::GetInstance()->DeleteValue(index[i]);
            }

            qvalue_t operator=(const qvalue_t &newValue);
            scalar operator=(scalar value)
            {
                operator=(qvalue_t::fromScalar(value));
                return value;
            }
            qvalue_t operator=(const QValue &newValue)
            {
                operator=(newValue.value);
                return newValue.value;
            }
            operator QValue() const;
            operator scalar() const { return (scalar) operator QValue(); }

            Elem<1> operator()(const QValue &m)
            {
                qvalue_t index[1] = {m};
                return Elem<1>((QValue) * this, index, Default);
            }

            Elem<2> operator()(const QValue &m, const QValue &n)
            {
                qvalue_t index[2] = {m, n};
                return Elem<2>((QValue) * this, index, Default);
            }

            Elem<3> operator()(const QValue &m, const QValue &n,
                               const QValue &k)
            {
                qvalue_t index[3] = {m, n, k};
                return Elem<3>((QValue) * this, index, Default);
            }

            Elem<1> operator()(int m)
            {
                qvalue_t index[1] = {qvalue_t::fromScalar(m)};
                return Elem<1>((QValue) * this, index, Default);
            }

            Elem<2> operator()(int m, int n)
            {
                qvalue_t index[2] = {qvalue_t::fromScalar(m),
                                     qvalue_t::fromScalar(n)};
                return Elem<2>((QValue) * this, index, Default);
            }

            Elem<3> operator()(int m, int n, int k)
            {
                qvalue_t index[3] = {qvalue_t::fromScalar(m),
                                     qvalue_t::fromScalar(n),
                                     qvalue_t::fromScalar(k)};
                return Elem<3>((QValue) * this, index, Default);
            }
        };

        // Halper class for element-wise operations
        class ElemWiseOp
        {
            qvalue_t value;

          public:
            ElemWiseOp(QValue &other)
            {
                this->value = other.value;
                IQuasarHost::GetInstance()->AddRef(value);
            }
            ~ElemWiseOp() { IQuasarHost::GetInstance()->DeleteValue(value); }

            operator QValue() const;
        };

        QValue(const qvalue_t &value) { this->value = value; }

        QValue(const QValue &other)
        {
            this->value = other.value;
            IQuasarHost::GetInstance()->AddRef(value);
        }
        QValue(LPCWSTR str)
        {
            value = IQuasarHost::GetInstance()->CreateString(str);
        }
        template <typename T, int N> QValue(T(&vals)[N])
        {
            value = IQuasarHost::GetInstance()->CreateVector(vals);
        }

        qvalue_t *operator->() { return &value; }
        const qvalue_t *operator->() const { return &value; }
        operator const qvalue_t &() const { return value; }
        operator scalar() const
        {
            _ASSERT(value.type == TYPE_SCALAR);
            return value.scalarVal;
        }
        qvalue_t &operator=(const QValue &other)
        {
            ReleaseRef();
            value = other.value;
            IQuasarHost::GetInstance()->AddRef(value);
            return value;
        }

        Elem<1> operator()(const QValue &m, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[1] = {m};
            return Elem<1>(value, index, accessMode);
        }

        Elem<2> operator()(const QValue &m, const QValue &n, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[2] = {m, n};
            return Elem<2>(value, index, accessMode);
        }

        Elem<3> operator()(const QValue &m, const QValue &n, const QValue &k, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[3] = {m, n, k};
            return Elem<3>(value, index, accessMode);
        }

		Elem<4> operator()(const QValue &m, const QValue &n, const QValue &k, const QValue &l, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[4] = {m, n, k, l};
            return Elem<4>(value, index, accessMode);
        }

		Elem<5> operator()(const QValue &m, const QValue &n, const QValue &k, const QValue &l, const QValue &o, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[5] = {m, n, k, l, o};
            return Elem<5>(value, index, accessMode);
        }

        Elem<1> operator()(int m, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[1] = {qvalue_t::fromScalar(m)};
            return Elem<1>(value, index, accessMode);
        }

        Elem<2> operator()(int m, int n, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[2] = {qvalue_t::fromScalar(m),
                                 qvalue_t::fromScalar(n)};
            return Elem<2>(value, index, accessMode);
        }

        Elem<3> operator()(int m, int n, int k, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[3] = {qvalue_t::fromScalar(m),
                                 qvalue_t::fromScalar(n),
                                 qvalue_t::fromScalar(k)};
            return Elem<3>(value, index, accessMode);
        }

		Elem<4> operator()(int m, int n, int k, int l, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[4] = {qvalue_t::fromScalar(m),
                                 qvalue_t::fromScalar(n),
                                 qvalue_t::fromScalar(k),
								 qvalue_t::fromScalar(l)};
			return Elem<4>(value, index, accessMode);
        }

		Elem<5> operator()(int m, int n, int k, int l, int o, BoundaryAccessMode accessMode = Default)
        {
            qvalue_t index[5] = {qvalue_t::fromScalar(m),
                                 qvalue_t::fromScalar(n),
                                 qvalue_t::fromScalar(k),
								 qvalue_t::fromScalar(l),
								 qvalue_t::fromScalar(o)};
			return Elem<5>(value, index, accessMode);
        }

		template <int N>
		Elem<N> operator()(intN<N> index, BoundaryAccessMode accessMode = Default)
		{
			qvalue_t ind[N];
			for (int i=0;i<N;i++)
				ind[i] = qvalue_t::fromScalar(index.el[i]);
			return Elem<N>(value, index, accessMode);
		}

        ElemWiseOp ElemWise() { return ElemWiseOp(*this); }

        void ReleaseRef() { IQuasarHost::GetInstance()->ReleaseRef(value); }
        // Note: the destructor is non-virtual to avoid frame pointer. Be aware
        // of possible memory leaks when a child object is released through a
        // base pointer. By design, all QValue derived instances should be such
        // that only the value is kept (and needs to be released).
        ~QValue() {
			IQuasarHost* host = IQuasarHost::GetInstance();
			if (host) host->DeleteValue(value);
		}

        void SetField(LPCWSTR fieldName, QValue value)
        {
            IQuasarHost::GetInstance()->SetField(this->value, fieldName, value);
        }

        QValue GetField(LPCWSTR fieldName)
        {
            qvalue_t y;
            IQuasarHost::GetInstance()->GetField(this->value, fieldName, y);
            return y;
        }

        template <typename T> static QValue CreateVector(int numel)
        {
            return IQuasarHost::GetInstance()->CreateVector<T>(numel);
        }

        // Creation of generic matrices
        template <typename T> static QValue CreateMatrix(int dim1, int dim2)
        {
            return IQuasarHost::GetInstance()->CreateMatrix<T>(dim1, dim2);
        }

        template <typename T>
        static QValue CreateMatrix(int dim1, int dim2, const T *vals)
        {
            return IQuasarHost::GetInstance()->CreateMatrix<T>(dim1, dim2,
                                                               vals);
        }

        // Creation of generic cubes
        template <typename T>
        static QValue CreateCube(int dim1, int dim2, int dim3)
        {
            return IQuasarHost::GetInstance()->CreateCube<T>(dim1, dim2, dim3);
        }

		// Creation of higher dimensional cubes
		template <typename T, int N>
		static QValue CreateNCube(intN<N> dims)
		{
			return IQuasarHost::GetInstance()->CreateNCube<T>(N, dims.el);
		}

        static QValue ReadHostVariable(LPCWSTR varName)
        {
            qvalue_t var;
            IQuasarHost::GetInstance()->ReadVariable(varName, var);
            return var;
        }

      public: // Operators
        QValue operator+(QValue b) { return BinaryOperation(*this, b, OP_ADD); }
        QValue operator-(QValue b) { return BinaryOperation(*this, b, OP_SUB); }
        QValue operator*(QValue b) { return BinaryOperation(*this, b, OP_MULTIPLY); }
        QValue operator/(QValue b) { return BinaryOperation(*this, b, OP_DIVIDE); }
        QValue operator-() { return UnaryOperation(*this, OP_NEGATE); }

		QValue operator+(float b) { return operator+(QValue(b)); }
        QValue operator-(float b) { return operator-(QValue(b)); }
		QValue operator*(float b) { return operator*(QValue(b)); }
        QValue operator/(float b) { return operator/(QValue(b)); }

		QValue operator+(double b) { return operator+(QValue(b)); }
        QValue operator-(double b) { return operator-(QValue(b)); }
		QValue operator*(double b) { return operator*(QValue(b)); }
        QValue operator/(double b) { return operator/(QValue(b)); }

        static QValue UnaryOperation(QValue a, OperatorTypes op)
        {
            IEvaluationStack *stack = IQuasarHost::GetInstance()
                                          ->GetComputationEngine()
                                          ->GetEvaluationStack();

            stack->PushValue(a);
            stack->Process(op);
            return stack->PopValue();
        }
        static QValue BinaryOperation(QValue a, QValue b, OperatorTypes op)
        {
            IEvaluationStack *stack = IQuasarHost::GetInstance()
                                          ->GetComputationEngine()
                                          ->GetEvaluationStack();

            stack->PushValue(a);
            stack->PushValue(b);
            stack->Process(op);
            return stack->PopValue();
        }
    };

    INLINE QValue operator*(QValue::ElemWiseOp a, QValue b)
    {
        return QValue::BinaryOperation(a, b, OP_PW_MULTIPLY);
    }


    // Function wrapper - allows you to call functions defined in quasar
    class Function : public QValue
    {
        LPCWSTR functionSignature;

      public:
        Function(const qvalue_t &value) : QValue(value)
        {
            _ASSERT(value.type == TYPE_LAMBDAEXPR);
        }

        // Construct a function based on the given signature. For example:
        // Function imshow("imshow(cube,vec)");
        // Note - the caller owns the reference to the string and is responsible
        // for it for the whole lifetime of the function. Usually this is not an
        // issue, as this constructed is intended to be used with constant
        // values.
        Function(LPCWSTR functionSignature)
        {
            this->functionSignature = functionSignature;
        }

        Function(const Function &other) : QValue(other) {}

        void BindFunction()
        {
            if (value.private_obj != NULL)
                return;

            const int N = 256;
            TCHAR func[N];
#ifdef _MSC_VER
            wcscpy_s(func, functionSignature);
#else
            wcsncpy(func, functionSignature, N);
            func[N - 1] = '\0';
#endif
            TCHAR *t = wcschr(func, '(');
            if (t)
                *t = '\0';

            // First, check if there is a variable that defines this function
            if (!IQuasarHost::GetInstance()->ReadVariable(func, value))
            {
                // Otherwise, check the reductions
                value = IQuasarHost::GetInstance()->LookupFunction(
                    functionSignature);

                _ASSERT(value.private_obj != NULL);
                // throw ex
            }
            _ASSERT(value.type == TYPE_LAMBDAEXPR);
        }

        QValue operator()(void)
        {
            qvalue_t retval;
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, NULL, 0, &retval,
                                                     1);
            return retval;
        }
        QValue operator()(QValue a)
        {
            qvalue_t retval;
            qvalue_t args[] = {a};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 1, &retval,
                                                     1);
            a.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue a, QValue b)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 2, &retval,
                                                     1);
            a.ReleaseRef();
            b.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue a, QValue b, QValue c)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b, c};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 3, &retval,
                                                     1);
            a.ReleaseRef();
            b.ReleaseRef();
            c.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue a, QValue b, QValue c, QValue d)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b, c, d};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 4, &retval,
                                                     1);
            a.ReleaseRef();
            b.ReleaseRef();
            c.ReleaseRef();
            d.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue a, QValue b, QValue c, QValue d, QValue e)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b, c, d, e};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 5, &retval,
                                                     1);
            a.ReleaseRef();
            b.ReleaseRef();
            c.ReleaseRef();
            d.ReleaseRef();
            e.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue a, QValue b, QValue c, QValue d, QValue e,
                          QValue f)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b, c, d, e, f};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 6, &retval,
                                                     1);
            a.ReleaseRef();
            b.ReleaseRef();
            c.ReleaseRef();
            d.ReleaseRef();
            e.ReleaseRef();
            f.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue a, QValue b, QValue c, QValue d, QValue e,
                          QValue f, QValue g)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b, c, d, e, f, g};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 7, &retval,
                                                     1);
            a.ReleaseRef();
            b.ReleaseRef();
            c.ReleaseRef();
            d.ReleaseRef();
            e.ReleaseRef();
            f.ReleaseRef();
            g.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue a, QValue b, QValue c, QValue d, QValue e,
                          QValue f, QValue g, QValue h)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b, c, d, e, f, g, h};
            BindFunction();
            IQuasarHost::GetInstance()->FunctionCall(value, args, 8, &retval,
                                                     1);
            a.ReleaseRef();
            b.ReleaseRef();
            c.ReleaseRef();
            d.ReleaseRef();
            e.ReleaseRef();
            f.ReleaseRef();
            g.ReleaseRef();
            h.ReleaseRef();
            return retval;
        }
    };

    // Provides information about Quasar types
    class Type : public QValue
    {
      public:
        Type(const qvalue_t &value) : QValue(value)
        {
            _ASSERT(value.type == TYPE_TYPEINFO);
        }
        Type(LPCWSTR typeName)
        {
            value = IQuasarHost::GetInstance()->LookupType(typeName);
            _ASSERT(value.private_obj != NULL);
        }
        Type(const Type &other) : QValue(other) {}
        template <int N> void ToString(TCHAR str[N])
        {
            // TODO
            // IQuasarHost::GetInstance()->TypeToString(str, N);
        }
    };

    class Method : public QValue
    {
      private:
        Type type;

      public:
        Method(const Type &type, LPCWSTR functionSignature) : type(type)
        {
            this->value = IQuasarHost::GetInstance()->LookupMethod(
                type, functionSignature);
            _ASSERT(value.type == TYPE_METHOD && value.private_obj != NULL);
        }
        QValue operator()(QValue target)
        {
            qvalue_t retval;
            IQuasarHost::GetInstance()->MethodCall(value, target, NULL, 0,
                                                   &retval, 1);
            target.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue target, QValue a)
        {
            qvalue_t retval;
            IQuasarHost::GetInstance()->MethodCall(
                value, target, &(qvalue_t &)a, 1, &retval, 1);
            target.ReleaseRef();
            a.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue target, QValue a, QValue b)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b};
            IQuasarHost::GetInstance()->MethodCall(value, target, args, 2,
                                                   &retval, 1);
            target.ReleaseRef();
            a.ReleaseRef();
            b.ReleaseRef();
            return retval;
        }
        QValue operator()(QValue target, QValue a, QValue b, QValue c)
        {
            qvalue_t retval;
            qvalue_t args[] = {a, b, c};
            IQuasarHost::GetInstance()->MethodCall(value, target, args, 3,
                                                   &retval, 1);
            target.ReleaseRef();
            a.ReleaseRef();
            b.ReleaseRef();
            c.ReleaseRef();
            return retval;
        }
    };

    class TypeBuilder : private QValue
    {
      public:
        TypeBuilder(LPCWSTR moduleName, LPCWSTR typeName)
        {
            this->value =
                IQuasarHost::GetInstance()->CreateType(moduleName, typeName);
        }
        void AddField(LPCWSTR fieldName, Type fieldType)
        {
            IQuasarHost::GetInstance()->AddField(
                this->value.private_obj, fieldName, fieldType->private_obj);
        }
        template <class T> Type CreateType()
        {
            IQuasarHost::GetInstance()->FinalizeType(value.private_obj);
            IQuasarHost::GetInstance()->AddRef(value);
            return Type(value);
        }
    };

    class Range : public QValue
    {
      public:
        Range() {}

        // Construct a sequence
        Range(scalar minVal, scalar maxVal, scalar step = 1)
        {
            IQuasarHost *instance = IQuasarHost::GetInstance();
            IComputationEngine *engine = instance->GetComputationEngine();
            IEvaluationStack *stack = engine->GetEvaluationStack();
            stack->PushValue(qvalue_t::fromScalar(minVal));
            stack->PushValue(qvalue_t::fromScalar(maxVal));
            stack->PushValue(qvalue_t::fromScalar(step));
            engine->Process(OP_DOTDOTDOT);
            value = stack->PopValue();
        }

		Range(int minVal, int maxVal, int step = 1)
        {
            IQuasarHost *instance = IQuasarHost::GetInstance();
            IComputationEngine *engine = instance->GetComputationEngine();
            IEvaluationStack *stack = engine->GetEvaluationStack();
            stack->PushValue(qvalue_t::fromInt(minVal));
            stack->PushValue(qvalue_t::fromInt(maxVal));
            stack->PushValue(qvalue_t::fromInt(step));
            engine->Process(OP_DOTDOTDOT);
            value = stack->PopValue();
        }
    };


    // The Quasar size function!
    INLINE int size(const QValue &val, int dim)
    {
        int len = 1;
        switch (val->type)
        {
        case TYPE_VEC:
        case TYPE_MAT:
        case TYPE_CUBE:
		case TYPE_NCUBE:
        case TYPE_CVEC:
        case TYPE_CMAT:
        case TYPE_CCUBE:
		case TYPE_NCCUBE:
			if (dim < 3)
			{
				len = (dim == 0) ? val->dim1 :
					  (dim == 1) ? val->dim2 :
					  (dim == 2) ? val->dim3 : 0;
			}
			else // Higher dimensional matrix
			{
				int dims[16];
				int ndims = 16;
				IQuasarHost::GetInstance()->GetNDims(val, &ndims, dims);
				len = (dim < ndims) ? dims[dim] : 0;
			}
            break;
        }
        return len;
    }

    // The quasar type function!
    INLINE Type type(const QValue &val)
    {
        return IQuasarHost::GetInstance()->GetType(val);
    }


    template <int N> QValue::Elem<N>::operator QValue() const
    {
        IQuasarHost *host = IQuasarHost::GetInstance();
        IComputationEngine *engine = host->GetComputationEngine();
        IEvaluationStack *stack = engine->GetEvaluationStack();

        for (int i = 0; i < N; i++)
            stack->PushValue(index[i]);
        engine->ArrayGetAt(mtx, N, boundaryAccessMode);
        return stack->PopValue();
    }

    template <int N>
    qvalue_t QValue::Elem<N>::operator=(const qvalue_t &newValue)
    {
        IQuasarHost *host = IQuasarHost::GetInstance();
        IComputationEngine *engine = host->GetComputationEngine();
        IEvaluationStack *stack = engine->GetEvaluationStack();

        for (int i = 0; i < N; i++)
            stack->PushValue(index[i]);
        stack->PushValue(newValue);
        engine->ArraySetAt(mtx, N, boundaryAccessMode);
        return newValue;
    }

    INLINE QValue::ElemWiseOp::operator QValue() const { return value; }

	// Provides cooperative memory resources (allocated by the Quasar host).
	// This class is used for implementing the cooperative memory model.
	template <typename T>
	class cooperative : public T
	{
	private:
		qvalue_t obj;
		LockingModes lockingMode;
		MemResources memResource;

	public:
		cooperative()
		{
			lockingMode = LOCK_READWRITE;
			memResource = MEMRESOURCE_CPU;
		}
		cooperative(T t, qvalue_t obj, LockingModes lockingMode = LOCK_READWRITE,
							    MemResources memResource = MEMRESOURCE_CPU)
		{
			(T &)*this = t;
			this->lockingMode = lockingMode;
			this->memResource = memResource;
			this->obj = obj;
		}
		cooperative(const cooperative & b)
		{
			InitFrom(b);
		}
		~cooperative()
		{
			Dispose();
		}
		cooperative & operator= (const cooperative & b)
		{
			Dispose();
			InitFrom(b);
			return *this;
		}
		operator qvalue_t() const { return obj; }
		operator QValue() { IQuasarHost::GetInstance()->AddRef(obj); return obj; }
	private:
		void InitFrom(const cooperative & b)
		{
			(T &)*this = (T) b;
			this->lockingMode = b.lockingMode;
			this->memResource = b.memResource;
			this->obj = b.obj;
			void *pData;

			IQuasarHost *host = IQuasarHost::GetInstance();
			host->AddRef(obj);
			host->Lock(obj, TYPE_VOID, lockingMode, memResource, &pData);
			// Note - we can ignore pData here, since the pointer is already stored in T
		}
		void Dispose()
		{
			IQuasarHost *host = IQuasarHost::GetInstance();
			if (obj.private_obj)
			{
				host->Unlock(obj, lockingMode, memResource);
				host->ReleaseRef(obj);
				obj.private_obj = NULL;
			}
		}
	};

	// Cooperative memory allocation functions
    // 1. uninit_coop function
	template <typename R>
	static cooperative<VectorBase<R> > uninit_coop(int M, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
		IQuasarHost *host = IQuasarHost::GetInstance();
		qvalue_t obj = host->CreateVector<R>(M);
		return cooperative<VectorBase<R> >(host->LockVector<R>(obj, lockingMode, memResource), obj, lockingMode, memResource);
	}
	template <typename R>
	static cooperative<MatrixBase<R> > uninit_coop(int M, int N, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
		IQuasarHost *host = IQuasarHost::GetInstance();
		qvalue_t obj = host->CreateMatrix<R>(M, N);
		return cooperative<MatrixBase<R> >(host->LockMatrix<R>(obj, lockingMode, memResource), obj, lockingMode, memResource);
	}
	template <typename R>
	static cooperative<CubeBase<R> > uninit_coop(int M, int N, int P, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
		IQuasarHost *host = IQuasarHost::GetInstance();
		qvalue_t obj = host->CreateCube<R>(M, N, P);
		return cooperative<CubeBase<R> >(host->LockCube<R>(obj, lockingMode, memResource), obj, lockingMode, memResource);
	}
	template <typename R, int N>
	static cooperative<NCubeBase<R, N> > uninit_coop(intN<N> dims, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
		IQuasarHost *host = IQuasarHost::GetInstance();
		qvalue_t obj = host->CreateNCube<R, N>(dims);
		return cooperative<NCubeBase<R, N> >(host->LockNCube<R, N>(obj, lockingMode, memResource), obj, lockingMode, memResource);
	}
    // 2. zeros_coop function
    template <typename R>
	static cooperative<VectorBase<R> > zeros_coop(int M, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<VectorBase<R> > y = uninit_coop<R>(M, lockingMode, memResource);
        mem_fill<R>((R *) y.data, M, 0);
        return y;
    }
    template <typename R>
	static cooperative<MatrixBase<R> > zeros_coop(int M, int N, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<MatrixBase<R> > y = uninit_coop<R>(M, N, lockingMode, memResource);
        mem_fill<R>((R *) y.data, M*N, 0);
        return y;
    }
    template <typename R>
	static cooperative<CubeBase<R> > zeros_coop(int M, int N, int P, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<CubeBase<R> > y = uninit_coop<R>(M, N, P, lockingMode, memResource);
        mem_fill<R>((R *) y.data, M*N*P, 0);
        return y;
    }
    template <typename R, int N>
	static cooperative<NCubeBase<R, N> > zeros_coop(intN<N> dims, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<NCubeBase<R, N> > y = uninit_coop<R, N>(dims, lockingMode, memResource);
        mem_fill<R>((R *) y.data, prod(dims), 0);
        return y;
    }
    // 3. ones_coop function
    template <typename R>
    static cooperative<VectorBase<R> > ones_coop(int M, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<VectorBase<R> > y = uninit_coop<R>(M, lockingMode, memResource);
        mem_fill<R>((R *) y.data, M, 1);
        return y;
    }
    template <typename R>
    static cooperative<MatrixBase<R> > ones_coop(int M, int N, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<MatrixBase<R> > y = uninit_coop<R>(M, N, lockingMode, memResource);
        mem_fill<R>((R *) y.data, M*N, 1);
        return y;
    }
    template <typename R>
    static cooperative<CubeBase<R> > ones_coop(int M, int N, int P, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<CubeBase<R> > y = uninit_coop<R>(M, N, P, lockingMode, memResource);
        mem_fill<R>((R *) y.data, M*N*P, 1);
        return y;
    }
    template <typename R, int N>
    static cooperative<NCubeBase<R, N> > ones_coop(intN<N> dims, LockingModes lockingMode = LOCK_READWRITE, MemResources memResource = MEMRESOURCE_CPU) {
        cooperative<NCubeBase<R, N> > y = uninit_coop<R, N>(dims, lockingMode, memResource);
        mem_fill<R>((R *) y.data, prod(dims), 1);
        return y;
    }

	// Quasar Core Library. Allows to easily call main Quasar functions by providing
	// static bindings to the library functions. Depending on the implementation, this
	// is basically a indirect function call.
	struct CoreLib
	{
		Function zeros, ones, uninit, uninit_T, czeros, rand, randn, cell, struct_,
				 object;
		Function size, numel, ndims;
		Function cast, complex, real, imag, transpose, copy, deepcopy, squeeze,
				 repmat, reshape, type, maxvalue, minvalue, new_, tic, toc, pause,
			     cell2mat, mat2cell, int2pos, fromascii, fromunicode, toascii,
				 tounicode;
		Function sum, dotprod, cumsum, prod, cumprod, mean, linspace;
		Function parallel_do, serial_do;
		Function imread, imwrite, fopen, fgetpos, fsetpos, fclose, dir;
		Function sprintf, sscanf, printf, fprintf, fprint, fread, fwrite, fscanf, fgets,
				 feof, print_, error_, assert_, strcat, factorial, eval, save, load;
		Function fft1, fft2, fft3, ifft1, ifft2, ifft3, irealfft1, irealfft2, irealfft3;
		Function max_block_size, opt_block_size;
		Function abs, asin, acos, atan, atan2, ceil, round, cos, sin, exp, exp2,
				 floor, mod, frac, log, log2, log10, lgamma, erf, erfc, max, min,
				 pow, saturate, sign, sqrt, rsqrt, tan, angle, conj, float_, int_,
				 isinf, isfinite, isnan, periodize, mirror_ext, clamp, sinh, cosh,
				 tanh, asinh, aconsh, atanh, and, or, xor, not, shl, shr;
		Function imshow, plot, scatter, disp, sync_framerate, hold, title, xlabel,
			     ylabel, xlim, ylim;

		CoreLib():
			zeros(L"zeros(...)"), // function with variable arguments
			ones(L"ones(...)"),
			uninit(L"uninit(...)"),
			uninit_T(L"uninit_T(??,...)"),
			czeros(L"czeros(...)"),
			rand(L"rand(...)"),
			randn(L"randn(...)"),
			cell(L"cell(...)"),
			struct_(L"struct()"),
			object(L"object()"),
			size(L"size(...)"),
			numel(L"numel(?\?)"),
			ndims(L"ndims(?\?)"),
			cast(L"cast(?\?,?\?)"),
			complex(L"complex(...)"),
			real(L"real(?\?)"),
			imag(L"imag(?\?)"),
			transpose(L"transpose(?\?)"),
			copy(L"copy(?\?)"),
			deepcopy(L"deepcopy(?\?)"),
			squeeze(L"squeeze(?\?)"),
			repmat(L"repmat(?\?,?\?)"),
			reshape(L"reshape(?\?,?\?)"),
			type(L"type(...)"),
			maxvalue(L"maxvalue(?\?)"),
			minvalue(L"minvalue(?\?)"),
			new_(L"new(?\?)"),
			tic(L"tic()"),
			toc(L"toc(...)"),
			pause(L"pause(?\?)"),
			cell2mat(L"cell2mat(?\?)"),
			mat2cell(L"mat2cell(?\?)"),
			int2pos(L"int2pos(?\?)"),
			fromascii(L"fromascii(?\?)"),
			fromunicode(L"fromunicode(?\?)"),
			toascii(L"toascii(?\?)"),
			tounicode(L"tounicode(?\?)"),
			sum(L"sum(...)"),
			dotprod(L"dotprod(?\?)"),
			cumsum(L"cumsum(...)"),
			prod(L"prod(...)"),
			cumprod(L"cumprod(...)"),
			mean(L"mean(...)"),
			linspace(L"linspace(?\?,?\?,?\?)"),
			parallel_do(L"parallel_do(...)"),
			serial_do(L"serial_do(...)"),
			imread(L"imread(?\?)"),
			imwrite(L"imwrite(?\?)"),
			fopen(L"fopen(?\?,?\?)"),
			fgetpos(L"fgetpos(?\?)"),
			fsetpos(L"fsetpos(?\?,?\?)"),
			fclose(L"fclose(?\?)"),
			dir(L"dir(?\?)"),
			sprintf(L"sprintf(...)"),
			sscanf(L"sscanf(...)"),
			printf(L"printf(...)"),
			fprintf(L"fprintf(...)"),
			fprint(L"fprint(...)"),
			fread(L"fread(...)"),
			fwrite(L"fwrite(...)"),
			fscanf(L"fscanf(...)"),
			fgets(L"fgets(?\?)"),
			feof(L"feof(?\?)"),
			print_(L"print(...)"),
			error_(L"error(...)"),
			assert_(L"assert(...)"),
			strcat(L"strcat(...)"),
			factorial(L"factorial(?\?)"),
			eval(L"eval(?\?)"),
			save(L"save(?\?)"),
			load(L"load(?\?)"),
			fft1(L"fft1(?\?)"),
			fft2(L"fft2(?\?)"),
			fft3(L"fft3(?\?)"),
			ifft1(L"ifft1(?\?)"),
			ifft2(L"ifft2(?\?)"),
			ifft3(L"ifft3(?\?)"),
			irealfft1(L"irealfft1(?\?)"),
			irealfft2(L"irealfft2(?\?)"),
			irealfft3(L"irealfft3(?\?)"),
			max_block_size(L"max_block_size(...)"),
			opt_block_size(L"opt_block_size(...)"),
			abs(L"abs(?\?)(?\?)"),
			asin(L"asin(?\?)"),
			acos(L"acos(?\?)"),
			atan(L"atan(?\?)"),
			atan2(L"atan2(?\?,?\?)"),
			ceil(L"ceil(?\?)"),
			round(L"round(?\?)"),
			cos(L"cos(?\?)"),
			sin(L"sin(?\?)"),
			exp(L"exp(?\?)"),
			exp2(L"exp2(?\?)"),
			floor(L"floor(?\?)"),
			mod(L"mod(?\?)"),
			frac(L"frac(?\?)"),
			log(L"log(?\?)"),
			log2(L"log2(?\?)"),
			log10(L"log10(?\?)"),
			lgamma(L"lgamma(?\?)"),
			erf(L"erf(?\?)"),
			erfc(L"erfc(?\?)"),
			max(L"max(...)"),
			min(L"min(...)"),
			pow(L"pow(?\?,?\?)"),
			saturate(L"saturate(?\?)"),
			sign(L"sign(?\?)"),
			sqrt(L"sqrt(?\?)"),
			rsqrt(L"rsqrt(?\?)"),
			tan(L"tan(?\?)"),
			angle(L"angle(?\?)"),
			conj(L"conj(?\?)"),
			float_(L"float(?\?)"),
			int_(L"int(?\?)"),
			isinf(L"isinf(?\?)"),
			isfinite(L"isfinite(?\?)"),
			isnan(L"isnan(?\?)"),
			periodize(L"periodize(?\?,?\?)"),
			mirror_ext(L"mirror_ext(?\?,?\?)"),
			clamp(L"clamp(?\?,?\?)"),
			sinh(L"sinh(?\?)"),
			cosh(L"cosh(?\?)"),
			tanh(L"tanh(?\?)"),
			asinh(L"asinh(?\?)"),
			aconsh(L"aconsh(?\?)"),
			atanh(L"atanh(?\?)"),
			and(L"and(?\?,?\?)"),
			or(L"or(?\?,?\?)"),
			xor(L"xor(?\?,?\?)"),
			not(L"not(?\?)"),
			shl(L"shl(?\?,?\?)"),
			shr(L"shr(?\?,?\?)"),
			imshow(L"imshow(...)"),
			plot(L"plot(...)"),
			scatter(L"scatter(...)"),
			disp(L"disp(...)"),
			sync_framerate(L"sync_framerate(...)"),
			hold(L"hold(...)"),
			title(L"title(...)"),
			xlabel(L"xlabel(...)"),
			ylabel(L"ylabel(...)"),
			xlim(L"xlim(...)"),
			ylim(L"ylim(...)")
		{}
	};


} // namespace quasar

#endif
