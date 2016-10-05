//=============================================================================
// Quasar CUDA / OpenMP runtime backend
// (C) 2011-2015 Bart Goossens
// Higher-dimensional extensions for the Quasar back-end
//=============================================================================
#include "quasar.h"

#ifndef QUASAR_NCUBE_H_INCLUDED
#define QUASAR_NCUBE_H_INCLUDED

namespace quasar
{
#ifdef ENABLE_DYNAMIC_KERNEL_MEM
#endif

    template <class T, int N> struct NCubeBase // Hyper-cube
    {
        T *data;
        intN<N> dims;
        INLINE __device__ int get_numel() const { return prod(dims); }

        INLINE __device__ NCubeBase() : data(NULL) {}
        INLINE __device__ NCubeBase(T *data, intN<N> dims)
            : data(data), dims(dims)
        {
        }
        INLINE __device__ void addref()
        {
#ifdef ENABLE_DYNAMIC_KERNEL_MEM
            if (dynmem->is_validpointer(data))
                dynmem->add_ref(data);
#endif
        }
        INLINE __device__ void release()
        {
#ifdef ENABLE_DYNAMIC_KERNEL_MEM
            if (dynmem->is_validpointer(data) && dynmem->release_ref(data) == 0)
            {
                dynmem->free(data);
            }
#endif
        }
    };

    template <int N>
    INLINE __device__ bool ncube_chkbnds(intN<N> pos, intN<N> dims)
    {
        bool ok = pos.el[0] >= 0 && pos.el[0] < dims.el[0];
        for (int i = 1; i < N; i++)
            ok &= pos.el[i] >= 0 && pos.el[i] < dims.el[i];
        return ok;
    }

    // Read functions
    template <class T, int N>
    INLINE __device__ T ncube_get_at(const NCubeBase<T, N> &x, intN<N> pos)
    {
		int index = pos2ind(x.dims, pos);
		CHECK_BOUNDS_FAST(x, index >= 0 && index < prod(x.dims), T());
        return x.data[index];
    }

	template <class T, class R, int N>
	INLINE __device__ R ncube_get_vec_at(const NCubeBase<T, N> &x, intN<N> pos)
	{
		int index = pos2ind(x.dims, pos);
		CHECK_BOUNDS_FAST(x, index >= 0 && index <= prod(x.dims) - ARRAY_LENGTH(R, T), R());
		return *(R *)&x.data[index];
	}

    template <class T, int N>
    INLINE __device__ T
    ncube_get_at_checked(const NCubeBase<T, N> &x, intN<N> pos)
    {
        CHECK_BOUNDS(x, ncube_chkbnds(pos, x.dims), T());
        return x.data[pos2ind(x.dims, pos)];
    }

    template <class T, int N>
    INLINE __device__ T ncube_get_at_safe(const NCubeBase<T, N> &x, intN<N> pos)
    {
        if (!ncube_chkbnds(pos, x.dims))
            return T();
        else
            return x.data[pos2ind(x.dims, pos)];
    }

    template <class T, int N>
    INLINE __device__ T ncube_get_at_circ(const NCubeBase<T, N> &x, intN<N> pos)
    {
        pos = periodize(pos, x.dims);
        return x.data[pos2ind(x.dims, pos)];
    }

    template <class T, int N>
    INLINE __device__ T ncube_get_at_mir(const NCubeBase<T, N> &x, intN<N> pos)
    {
        pos = mirror_ext(pos, x.dims);
        return x.data[pos2ind(x.dims, pos)];
    }

    template <int N>
    INLINE __device__ intN<N> clamp_ex(intN<N> pos, intN<N> dims)
    {
        intN<N> val;
        for (int i = 0; i < N; i++)
            val.el[i] = clamp(pos.el[i], dims.el[i] - 1);
        return val;
    }

    template <class T, int N>
    INLINE __device__ T
    ncube_get_at_clamped(const NCubeBase<T, N> &x, intN<N> pos)
    {
        pos = clamp_ex(pos, x.dims);
        return x.data[pos2ind(x.dims, pos)];
    }

    // Write functions
    template <class T, int N>
    INLINE __device__ T
    ncube_set_at(const NCubeBase<T, N> &x, intN<N> pos, T val)
    {
		int index = pos2ind(x.dims, pos);
		CHECK_BOUNDS_FAST(x, index >= 0 && index < prod(x.dims), T());
        CHECK_NAN_OR_INF(x, val);
        x.data[index] = val;
        return val;
    }

    template <class T, class R, int N>
    INLINE __device__ R
    ncube_set_vec_at(const NCubeBase<T, N> &x, intN<N> pos, R val)
    {
		int index = pos2ind(x.dims, pos);
		CHECK_BOUNDS_FAST(x, index >= 0 && index <= prod(x.dims) - ARRAY_LENGTH(R, T), R());
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[pos2ind(x.dims, pos)] = val;
        return val;
    }

    template <class T, int N>
    INLINE __device__ T
    ncube_set_at_checked(const NCubeBase<T, N> &x, intN<N> pos, T val)
    {
        CHECK_BOUNDS(x, ncube_chkbnds(pos, x.dims), T());
        CHECK_NAN_OR_INF(x, val);
        x.data[pos2ind(x.dims, pos)] = val;
        return val;
    }
    template <class T, class R, int N>
    INLINE __device__ R
    ncube_set_vec_at_checked(const NCubeBase<T, N> &x, intN<N> pos, R val)
    {
        CHECK_BOUNDS(x, ncube_chkbnds(pos, x.dims), R());
        CHECK_NAN_OR_INF(x, val);
        *(R *)&x.data[pos2ind(x.dims, pos)] = val;
        return val;
    }

    template <class T, int N>
    INLINE __device__ T
    ncube_set_at_safe(const NCubeBase<T, N> &x, intN<N> pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        if (ncube_chkbnds(pos, x.dims))
            x.data[pos2ind(x.dims, pos)] = val;
        return val;
    }

    template <class T, class R, int N>
    INLINE __device__ R
    ncube_set_vec_at_safe(const NCubeBase<T, N> &x, intN<N> pos, R val)
    {
        CHECK_BOUNDS(x, ncube_chkbnds(pos, x.dims), R());
        CHECK_NAN_OR_INF(x, val);
        if (ncube_chkbnds(pos, x.dims))
            *(R *)&x.data[pos2ind(x.dims, pos)] = val;
        return val;
    }

    template <class T, int N>
    INLINE __device__ T &ncube_get_ref_at(const NCubeBase<T, N> &x, intN<N> pos)
    {
        return x.data[pos2ind(x.dims, pos)];
    }

    template <class T, int N>
    INLINE __device__ T &ncube_get_ref_at_checked(const NCubeBase<T, N> &x,
                                                  intN<N> pos)
    {
        CHECK_BOUNDS(x, pos2ind(x.dims, pos), C_NULL_REF(T));
        return x.data[pos2ind(x.dims, pos)];
    }

    /// get_ref functions
    template <class T, int N>
    INLINE __device__ T &ncube_get_ref_at_safe(const NCubeBase<T, N> &x,
                                               intN<N> pos)
    {
        if (ncube_chkbnds(pos, x.dims))
            return C_NULL_REF(T);
        else
            return x.data[pos2ind(x.dims, pos)];
    }
    template <class T, int N>
    INLINE __device__ T &ncube_get_ref_at_circ(const NCubeBase<T, N> &x,
                                               intN<N> pos)
    {
        pos = periodize(pos, x.dims);
        return x.data[pos2ind(x.dims, pos)];
    }
    template <class T, int N>
    INLINE __device__ T &ncube_get_ref_at_mir(const NCubeBase<T, N> &x,
                                              intN<N> pos)
    {
        pos = mirror_ext(pos, x.dims);
        return x.data[pos2ind(x.dims, pos)];
    }
    template <class T, int N>
    INLINE __device__ T &ncube_get_ref_at_clamped(const NCubeBase<T, N> &x,
                                                  intN<N> pos)
    {
        pos = clamp_ex(pos, x.dims);
        return x.data[pos2ind(x.dims, pos)];
    }


    // Atomic operations
    template <class T, int N, T atomic_function(T *, T)>
    INLINE __device__ T ncube_at(const NCubeBase<T, N> &x, intN<N> pos, T val)
    {
        CHECK_NAN_OR_INF(x, val);
        return atomic_function((T *)&x.data[pos2ind(x.dims, pos)], val);
    }

    template <class T, int N, T atomic_function(T *, T)>
    INLINE __device__ T
    ncube_at_safe(const NCubeBase<T, N> &x, intN<N> pos, T val)
    {
        T retVal;
        CHECK_NAN_OR_INF(x, val);
        if (ncube_chkbnds(pos, x.dims))
            retVal = atomic_function((T *)&x.data[pos2ind(x.dims, pos)], val);
        return retVal;
    }

    template <class T, int N, T atomic_function(T *, T)>
    INLINE __device__ T
    ncube_at_checked(const NCubeBase<T, N> &x, intN<N> pos, T val)
    {
        CHECK_BOUNDS(x, ncube_chkbnds(pos, x.dims), T());
        CHECK_NAN_OR_INF(x, val);
        return atomic_function((T *)&x.data[pos2ind(x.dims, pos)], val);
    }

//===================================
// Specializations for cell matrices
//===================================
#ifdef ENABLE_DYNAMIC_KERNEL_MEM
#define DYNMEM_FUNC()                                                          \
    INLINE __device__ void addref()                                            \
    {                                                                          \
        if (dynmem->is_validpointer(data))                                     \
            dynmem->add_ref(data);                                             \
    }                                                                          \
    INLINE __device__ void release()                                           \
    {                                                                          \
        if (dynmem->is_validpointer(data) && dynmem->release_ref(data) == 0)   \
        {                                                                      \
            for (int i = 0; i < get_numel(); i++)                              \
                data[i].release();                                             \
            dynmem->free(data);                                                \
        }                                                                      \
    }
#else
#define DYNMEM_FUNC()                                                          \
    INLINE __device__ void addref() {}                                         \
    INLINE __device__ void release() {}
#endif


    template <class T, int N> struct VectorBase<NCubeBase<T, N> >
    {
        NCubeBase<T, N> *data;
        int dim1;

        INLINE __device__ VectorBase() : data(NULL) {}
        INLINE __device__ VectorBase(NCubeBase<T, N> *data, int dim1)
            : data(data), dim1(dim1)
        {
        }
        INLINE __device__ int get_numel() const { return dim1; }
        DYNMEM_FUNC()
    };

    template <class T, int N> struct MatrixBase<NCubeBase<T, N> >
    {
        NCubeBase<T, N> *data;
        int dim1;
        int dim2;

        INLINE __device__ MatrixBase() : data(NULL) {}
        INLINE __device__ MatrixBase(NCubeBase<T, N> *data, int dim1, int dim2)
            : data(data), dim1(dim1), dim2(dim2)
        {
        }
		INLINE __device__ int get_numel() const { return dim1 * dim2; }
        DYNMEM_FUNC()
    };

    template <class T, int N> struct CubeBase<NCubeBase<T, N> >
    {
        NCubeBase<T, N> *data;
        int dim1;
        int dim2;
        int dim3;

        INLINE __device__ CubeBase() : data(NULL) {}
        INLINE __device__
        CubeBase(NCubeBase<T, N> *data, int dim1, int dim2, int dim3)
            : data(data), dim1(dim1), dim2(dim2), dim3(dim3)
        {
        }
		INLINE __device__ int get_numel() const { return dim1 * dim2 * dim3; }
        DYNMEM_FUNC()
    };

    template <class T, int N> struct NCubeBase<VectorBase<T>, N>
    {
        VectorBase<T> *data;
        intN<N> dims;

        INLINE __device__ NCubeBase() : data(NULL) {}
        INLINE __device__ NCubeBase(VectorBase<T> *data, intN<N> dims)
            : data(data), dims(dims)
        {
        }
		INLINE __device__ int get_numel() const { return prod(dims); }
        DYNMEM_FUNC()
    };

    template <class T, int N> struct NCubeBase<MatrixBase<T>, N>
    {
        MatrixBase<T> *data;
        intN<N> dims;

        INLINE __device__ NCubeBase() : data(NULL) {}
        INLINE __device__ NCubeBase(MatrixBase<T> *data, intN<N> dims)
            : data(data), dims(dims)
        {
        }
		INLINE __device__ int get_numel() const { return prod(dims); }
        DYNMEM_FUNC()
    };

    template <class T, int N> struct NCubeBase<CubeBase<T>, N>
    {
        CubeBase<T> *data;
        intN<N> dims;

        INLINE __device__ NCubeBase() : data(NULL) {}
        INLINE __device__ NCubeBase(CubeBase<T> *data, intN<N> dims)
            : data(data), dims(dims)
        {
        }
		INLINE __device__ int get_numel() const { return prod(dims); }
        DYNMEM_FUNC()
    };

    template <class T, int M, int N> struct NCubeBase<NCubeBase<T, M>, N>
    {
        NCubeBase<T, M> *data;
        intN<N> dims;

        INLINE __device__ NCubeBase() : data(NULL) {}
        INLINE __device__ NCubeBase(NCubeBase<T, M> *data, intN<N> dims)
            : data(data), dims(dims)
        {
        }
		INLINE __device__ int get_numel() const { return prod(dims); }
        DYNMEM_FUNC()
    };

#undef DYNMEM_FUNC

    template <class T, int N>
    INLINE __device__ NCubeBase<T, N> make_ncube(T *data, intN<N> dims)
    {
        NCubeBase<T, N> c;
        c.dims = dims;
        c.data = data;
        return c;
    }

    //===================================
    // Shared memory allocation
    //===================================
    template <class T, int N>
    INLINE __device__ NCubeBase<T, N> shmem_alloc(shmem *_shmem, intN<N> dims)
    {
        return NCubeBase<T, N>(shmem_allocate<T>(_shmem, prod(dims)), dims);
    }

    template <class T>
    INLINE __device__ NCubeBase<T, 4> shmem_alloc(shmem *_shmem, int4 dims)
    {
        return shmem_alloc<T, 4>(_shmem, (intN<4>)dims);
    }

    template <class T, int N>
    INLINE __device__ NCubeBase<T, N> shmem_zeros(shmem *_shmem, intN<N> dims)
    {
        int n = prod(dims);
        NCubeBase<T, N> c = make_ncube(shmem_allocate<T>(_shmem, n), dims);
        shmem_fill<T>(c.data, n, 0.0f);
        return c;
    }

    template <class T>
    INLINE __device__ NCubeBase<T, 4> shmem_zeros(shmem *_shmem, int4 dims)
    {
        return shmem_zeros<T, 4>(_shmem, (intN<4>)dims);
    }

//===================================
// Dynamic memory allocation
//===================================
#ifdef ENABLE_DYNAMIC_KERNEL_MEM

    template <class T, int N>
    INLINE __device__ NCubeBase<T, N> uninit(const char *elemT, intN<N> dims)
    {
        return NCubeBase<T, N>(
            (T *)dynmem->allocate_addref(prod(dims) * sizeof(T)), dims);
    }

    template <class T>
    INLINE __device__ NCubeBase<T, 4> uninit(const char *elemT, int4 dims)
    {
        return uninit<T, 4>(elemT, (intN<4>)dims);
    }

    template <class T, int N>
    INLINE __device__ NCubeBase<T, N> zeros(const char *elemT, intN<N> dims)
    {
        NCubeBase<T, N> c = uninit<T, N>(elemT, dims);
        mem_fill<T>((T *)c.data, prod(dims), T());
        return c;
    }

    template <class T>
    INLINE __device__ NCubeBase<T, 4> zeros(const char *elemT, int4 dims)
    {
        return zeros<T, 4>(elemT, (intN<4>)dims);
    }

    template <class T, int N>
    INLINE __device__ NCubeBase<T, N> ones(const char *elemT, intN<N> dims)
    {
        NCubeBase<T, N> c = uninit<T, N>(elemT, dims);
        mem_fill<T>((T *)c.data, prod(dims), 1);
        return c;
    }

    template <class T>
    INLINE __device__ NCubeBase<T, 4> ones(const char *elemT, int4 dims)
    {
        return ones<T, 4>(elemT, (intN<4>)dims);
    }

#endif

	template <int N> struct NCube : public NCubeBase<scalar, N> {};
	template <int N> struct NCCube : public NCubeBase<cscalar, N> {};
} // namespace quasar

// QUASAR_NCUBE_H_INCLUDED
#endif
