public class bla {
    public double predict(double[][] sypet_arg0, double sypet_arg1) throws Throwable{
        org.apache.commons.math.stat.regression.SimpleRegression var_0 =  new org.apache.commons.math.stat.regression.SimpleRegression();
        var_0.addData(sypet_arg0);
        var_0.addData(sypet_arg0);
        double var_1 = var_0.predict(sypet_arg1);
        return var_1;
    }

    public boolean test1() throws Throwable {
        double[][] known = new double[][] {{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}};
        double x = 1.5;
        return predict(known, x) == 2.5;
    }

    public boolean test2() throws Throwable {
        double[][] known = new double[][] {{1, 3}, {2, 4}, {3, 5}, {4, 6}, {5, 7}};
        double x = 2.5;
        return predict(known, x) == 4.5;
    }

    public boolean test() throws Throwable {
        return test1() && test2();
    }

    public static void main(String[] args) throws Throwable {
        System.out.println(new bla().test());
    }

}
