# File to hold global decorators, adapted from the index of python enhancement proposals (PEP)


def accepts(*types):
    """
    Validate inputs by whether the args to a method inherit from the specified types

    :param types: tuple of types that have a one-to-one mapping to arguments for validation
    :return: function wrapper
    """
    def wrapper(func):
        from inspect import signature
        if len(types) != len(signature(func).parameters):
            raise ValueError("len(args) ({}) != method signature len(params) ({})".format(len(types), len(signature(func).parameters)))

        def accepts_func(*args, **kwargs):
            for (arg, arg_type) in zip(args, types):
                if not isinstance(arg, arg_type):
                    raise ValueError("arg {} of type {} is not an instance of {}".format(arg, type(arg), arg_type))
            return func(*args, **kwargs)
        accepts_func.__name__ = func.__name__
        return accepts_func
    return wrapper


def returns(return_type):
    """
    Validate return type by whether the return value inherits from the specified type

    :param return_type: the type of which the wrapped function must return or any type that inherits from return_type
    :return: function wrapper
    """
    def wrapper(func):
        def returns_func(*args, **kwargs):
            result = func(*args, **kwargs)
            if not isinstance(result, return_type):
                raise ValueError("return value {} of type {} is not an instance of {}".format(result, type(result), return_type))
            return result
        returns_func.__name__ = func.__name__
        return returns_func
    return wrapper
