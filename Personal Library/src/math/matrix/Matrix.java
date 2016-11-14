package math.matrix;

import math.BigDecimal_INF;

public class Matrix extends AbstractMatrix<Double , Matrix> {
    private int rank;
    
    public Matrix(Double[][] matrix) {
        this(matrix , false);
    }
    
    public Matrix(int width , int height) {
        this(new Double[width][height] , false , false);
    }
    
    public Matrix(Double[][] matrix , boolean isImmutable) {
        this(matrix , isImmutable , true);
    }
    
    private Matrix(Double[][] matrix , boolean isImmutable , boolean findRank) {
        super(matrix , isImmutable);
        if(findRank)
            findRank();
    }
    
    public int getRank() {
        return rank;
    }
    
    private int findRank() {
        if(isSquare() && det() != 0)
            return rank = numOfRow;
        else if(isSquare() && numOfRow == 1)
            return rank = 0;
        else {
            int size = Math.min(numOfCol , numOfRow - 1);
            rank = sub(0 , 0 , size , size).rank;
            
            if(rank == 0) {
                for(int i = 0; i < numOfRow; i++) {
                    for(int j = 0; j < numOfCol; j++)
                        if(get(i , j) != 0) {
                            rank = 1;
                        }
                }
            }
            
            return rank;
        }
    }
    
    public Matrix sub(int ix , int iy , int nor , int noc) {
        Matrix m = new Matrix(nor , noc);
        for(int x = 0; x < nor; x++) {
            for(int y = 0; y < nor; y++) {
                m.set(x , y , get(ix + x , iy + y));
            }
        }
        m.findRank();
        m.setImmutable();
        return m;
    }
    
    @Override
    public Matrix add(Matrix matrix) {
        if(matrix.numOfCol != numOfCol || matrix.numOfRow != numOfRow)
            throw new IllegalArgumentException();
        
        Double[][] sum = new Double[numOfCol][numOfRow];
        
        for(int i = 0; i < numOfCol; i++) {
            for(int j = 0; j < numOfRow; j++) {
                sum[i][j] = get(i , j) + matrix.get(i , j);
            }
        }
        
        return new Matrix(sum);
    }
    
    @Override
    public Matrix subtract(Matrix matrix) {
        if(matrix.numOfCol != numOfCol || matrix.numOfRow != numOfRow)
            throw new IllegalArgumentException();
        
        Double[][] sum = new Double[numOfCol][numOfRow];
        
        for(int i = 0; i < numOfCol; i++) {
            for(int j = 0; j < numOfRow; j++) {
                sum[i][j] = get(i , j) - matrix.get(i , j);
            }
        }
        
        return new Matrix(sum);
    }
    
    @Override
    public Matrix multiply(Double scalar) {
        Double[][] product = new Double[numOfCol][numOfRow];
        
        for(int i = 0; i < numOfCol; i++) {
            for(int j = 0; j < numOfRow; j++) {
                product[i][j] = scalar * get(i , j);
            }
        }
        
        return new Matrix(product);
    }
    
    @Override
    public Matrix multiply(Matrix matrix) {
        if(matrix.numOfRow != numOfCol) {
            throw new UnsupportedOperationException();
        }
        
        Double[][] product = new Double[numOfRow][matrix.numOfCol];
        
        for(int i = 0; i < numOfRow; i++) {
            for(int j = 0; j < matrix.numOfCol; j++) {
                product[i][j] = 0d;
                for(int ii = 0; ii < numOfCol; ii++) {
                    product[i][j] += get(i , ii) * matrix.get(ii , j);
                }
            }
        }
        
        return new Matrix(product);
    }
    
    @Override
    public Matrix pow(int pow) {
        if(!isSquare())
            throw new UnsupportedOperationException();
        if(pow == 0) {
            return new IdentityMatrix(numOfCol);
        }
        if(pow < 0) {
            return invert().pow(-pow);
        }
        Matrix matrix = this;
        
        for(int i = 0; i < pow; i++) {
            matrix = matrix.multiply(this);
        }
        
        return matrix;
    }
    
    @Override
    public Double det() {
        if(!isSquare()) {
            throw new UnsupportedOperationException();
        }
        
        if(numOfCol == 0)
            return 0d;
        if(numOfCol == 1)
            return get(0 , 0);
        if(numOfCol == 2)
            return get(0 , 0) * get(1 , 1) - get(1 , 0) * get(0 , 1);
        
        double det = 0;
        
        for(int i = 0; i < numOfCol; i++) {
            det += Math.pow(-1 , i) * get(0 , i) * adj(0 , i).det();
        }
        
        return det;
    }
    
    @Override
    public Matrix negate() {
        return multiply(-1d);
    }
    
    @Override
    public Matrix transpose() {
        Double[][] matrix = new Double[numOfRow][numOfCol];
        
        for(int i = 0; i < numOfCol; i++) {
            for(int j = 0; j < numOfRow; j++) {
                matrix[j][i] = get(i , j);
            }
        }
        
        return new Matrix(matrix);
    }
    
    @Override
    public Matrix adj(int row , int col) {
        Double[][] matrix = new Double[numOfCol - 1][numOfRow - 1];
        
        for(int i = 0; i < numOfCol; i++) {
            for(int j = 0; j < numOfRow; j++) {
                if(i != row && j != col)
                    matrix[i - (i > row ? 1 : 0)][j - (j > col ? 1 : 0)] = get(i , j);
            }
        }
        
        return new Matrix(matrix);
    }
    
    @Override
    public Matrix adj() {
        Double[][] adj = new Double[numOfCol][numOfRow];
        Matrix m = this.transpose();
        
        for(int i = 0; i < numOfCol; i++) {
            for(int j = 0; j < numOfRow; j++) {
                adj[i][j] = Math.pow(-1 , i + j) * m.adj(i , j).det();
            }
        }
        
        return new Matrix(adj);
    }
    
    @Override
    public Matrix invert() {
        return adj().multiply(1 / det());
    }
    
    public Matrix rref() {
        Matrix m = new Matrix(numOfRow , numOfCol);
        
        //Make columns cumulative
        for(int i = numOfRow - 1; i >= 0; i--) {
            for(int j = 0; j < numOfCol; j++) {
                double n = get(i , j);
                if(i < numOfRow - 1) {
                    n = m.get(i + 1 , j) + n;
                }
                m.set(i , j , n);
            }
        }
        
        //Go down matrix
        double pivotValue;
        
        for(int i = 0; i < numOfRow - 1; i++) {
            if((pivotValue = m.get(i , i)) == 0)
                continue;
            for(int j = i + 1; j < numOfRow; j++) {
                double c = -m.get(j , i) / pivotValue;
                for(int k = 0; k < numOfCol; k++)
                    m.set(j , k , m.get(i , k) * c + m.get(j , k));
            }
        }
        
        //Set diagonal to one
        for(int i = 0; i < numOfRow; i++) {
            double diag = m.get(i , i);
            if(diag == 0)
                continue;
            for(int j = 0; j < numOfCol; j++)
                m.set(i , j , m.get(i , j) / diag);
        }
        
        //Go up matrix
        double[] row = new double[numOfCol];
        for(int i = numOfRow - 2; i >= 0; i--) {
            for(int j = 0; j < numOfCol; j++)
                row[j] = m.get(i , j);
            for(int j = 0; j < numOfCol; j++)
                for(int k = 1; i + k < numOfRow; k++)
                    m.set(i , j , m.get(i , j) - m.get(i + k , j) * row[i + k]);
        }
        
        return m;
    }
    
    @Override
    public String toString() {
        if(numOfRow == 0 || numOfCol == 0) {
            return "[ ]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int j = 0; j < numOfRow; j++) {
            if(j != 0)
                builder.append(" ");
            builder.append("[");
            for(int i = 0; i < numOfCol; i++) {
                builder.append(get(j , i));
                if(i != numOfCol - 1)
                    builder.append(", ");
            }
            builder.append("]");
            if(j != numOfRow - 1)
                builder.append("\n");
            else
                builder.append("]");
        }
        return builder.toString();
    }
    
    public static Matrix createIdentityMatrix(int size) {
        return new IdentityMatrix(size);
    }
    
    private static class IdentityMatrix extends Matrix {
        public IdentityMatrix(int size) {
            super(create(size));
        }
        
        private static Double[][] create(int size) {
            Double[][] matrix = new Double[size][size];
            for(int i = 0; i < size; i++)
                matrix[i][i] = 1.;
            return matrix;
        }
    }
}
