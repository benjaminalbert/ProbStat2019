# File to hold useful decorators, often retrieved from the index of python enhancement proposals (PEP)

def accepts(*types):
    def check_accepts(f):
        assert len(types) == f.func_code.co_argcount
        def new_f(*args, **kwds):
            for (a, t) in zip(args, types):
                assert isinstance(a, t), "arg {} does not match {}".format(a, t)
            return f(*args, **kwds)
        new_f.func_name = f.func_name
        return new_f
    return check_accepts


def returns(rtype):
    def check_returns(f):
        def new_f(*args, **kwds):
            result = f(*args, **kwds)
            assert isinstance(result, rtype), "return value {} does not match {}".format(result, rtype)
            return result
        new_f.func_name = f.func_name
        return new_f
    return check_returns
