package test;


import java.io.IOException;


import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class Math3 {
	//求逆函数
    public static RealMatrix inverseMatrix(RealMatrix A) {
        RealMatrix result = new LUDecomposition(A).getSolver().getInverse();
        return result; 
    }
	public static void main(String[] args) throws IOException {
		double[][] b=new double[2][2];
		double[][] a=new double[2][2];
		b[0][0]=1;b[0][1]=0;
		b[1][0]=5;b[1][1]=1;
		a[0][0]=2;a[0][1]=4;
		a[1][0]=2;a[1][1]=3;
		RealMatrix rmb= new Array2DRowRealMatrix(b);
		RealMatrix rma =  new Array2DRowRealMatrix(a);
        rma=rma.multiply(rmb);
        RealMatrix inversetestMatrix = inverseMatrix(rma);
        System.out.println("逆矩阵为：\t"+inversetestMatrix);
        //矩阵转化为数组 getdata
        double matrixtoarray[][]=inversetestMatrix.getData();
        System.out.println("数组中的某一个数字为：\t"+matrixtoarray[0][1]);
      
	}

}
