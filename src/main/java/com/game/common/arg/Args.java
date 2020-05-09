package com.game.common.arg;

public class Args {

    public static <T0, T1> Args.Two<T0, T1> create(T0 arg0, T1 arg1){
        return new Two<>(arg0, arg1);
    }

    public static final class Two<T0, T1>{
        public final T0 arg0;
        public final T1 arg1;

        private Two(T0 arg0, T1 arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        public Two<T0, T1> clone(){
            return new Two<>(arg0, arg1);
        }

        public boolean eitherNull(){
            return arg0 == null || arg1 == null;
        }
    }
}
