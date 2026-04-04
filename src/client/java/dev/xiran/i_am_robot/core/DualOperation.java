package dev.xiran.i_am_robot.core;

@FunctionalInterface
public interface DualOperation<T, U> {
    Object apply(T arg0, U arg1);
}
