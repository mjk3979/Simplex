import java.math.BigInteger;

public class Simplex
{
	static class Rational implements Comparable<Rational>
	{
		public final int top;
		public final int bottom;

		public Rational()
		{
			this.top = 0;
			this.bottom = 1;
		}

		public Rational(int top, int bottom)
		{
			if (bottom < 0)
			{
				top *= -1;
				bottom *= -1;
			}
			int gcd = 1;
			if (top != 0)
				gcd = BigInteger.valueOf(top).gcd(BigInteger.valueOf(bottom)).intValue();
			this.top = top / gcd;
			this.bottom = bottom / gcd;
		}

		public Rational add(Rational o)
		{
			return new Rational(this.top * o.bottom + this.bottom * o.top, this.bottom * o.bottom);
		}

		public Rational subtract(Rational o)
		{
			return new Rational(this.top * o.bottom - this.bottom * o.top, this.bottom * o.bottom);
		}

		public Rational multiply(Rational o)
		{
			return new Rational(this.top * o.top, this.bottom * o.bottom);
		}

		public Rational divide(Rational o)
		{
			if (o.top == 0)
				return new Rational(999999, 1).multiply(this);
			return new Rational(this.top * o.bottom, this.bottom * o.top);
		}

		public int compareTo(Rational o)
		{
			Rational diff = this.subtract(o);
			if (diff.top < 0)
				return -1;
			else if (diff.top == 0)
				return 0;
			else
				return 1;
		}
		
		public boolean equals(Object o)
		{
			if (!(o instanceof Rational))
				return false;
			Rational other = (Rational)o;
			return this.compareTo(other) == 0;
		}

		public double toFloat()
		{
			if (bottom == 0)
				return Double.POSITIVE_INFINITY;
			return (double)top / bottom;
		}
	}

	static final Rational ZERO = new Rational(0, 1);

	public static int find_pcol(Rational obj[])
	{
		int min = -1;
		for (int i=0;i<obj.length;++i)
			if (obj[i].compareTo(ZERO) < 0 && (min == -1 || obj[i].compareTo(obj[min]) < 0))
				min = i;
		return min;
	}

	public static int find_prow(Rational mat[][], Rational cons[], int pcol)
	{
		int brow = 0;
		Rational best = cons[0].divide(mat[0][pcol]);
		for (int r=1; r < cons.length-1; ++r)
		{
			Rational ratio = cons[r].divide(mat[r][pcol]);
			if (ratio.compareTo(best) < 0)
			{
				best = ratio;
				brow = r; 
			}
		}
		return brow;
	}

	public static void pivot(Rational obj[], Rational cons[], Rational mat[][], int pcol, int prow)
	{
		Rational e = mat[prow][pcol];
		for (int c=0; c < mat[0].length; ++c)
			mat[prow][c] = mat[prow][c].divide(e);
		cons[prow] = cons[prow].divide(e);

		for (int r=0; r < mat.length; ++r)
		{
			if (r == prow)
				continue;
			Rational C = mat[r][pcol];
			for (int c=0; c < mat[r].length; ++c)
				mat[r][c] = mat[r][c].subtract(C.multiply(mat[prow][c]));
			cons[r] = cons[r].subtract(C.multiply(cons[prow]));
		}

		Rational C = obj[pcol];
		for (int c=0; c < obj.length; ++c)
			obj[c] = obj[c].subtract(C.multiply(mat[prow][c]));
		cons[cons.length-1] = cons[cons.length-1].subtract(C.multiply(cons[prow]));
	}

	public static void simplex(Rational obj[], Rational cons[], Rational mat[][])
	{
		while (true)
		{
			display(obj, cons, mat);
			System.out.println();

			int pcol = find_pcol(obj);
			if (pcol == -1)
				break;

			int prow = find_prow(mat, cons, pcol);

			pivot(obj, cons, mat, pcol, prow);
		}
	}

	public static void display(Rational obj[], Rational cons[], Rational mat[][])
	{
		for (int r=0; r<mat.length; ++r)
		{
			for (int c=0; c<mat[0].length; ++c)
			{
				System.out.printf("%10.2f  ", mat[r][c].toFloat());
			}
			System.out.printf("%10.2f\n", cons[r].toFloat());
		}

		for (int c=0; c<obj.length; ++c)
		{
			System.out.printf("%10.2f  ", obj[c].toFloat());
		}

		System.out.printf("%10.2f\n", cons[cons.length-1].toFloat());
	}

	public static void main(String[] args)
	{
		int num_vars = 3;
		int num_cons = 2;

		int obj[] = new int[num_vars + num_cons + 1];
		obj[0] = -2;
		obj[1] = -3;
		obj[2] = -4;
		obj[3] = 0;
		obj[4] = 0;
		//obj[5] = 0;
		obj[5] = 1;

		int cons_vals[] = new int[num_cons+1];
		cons_vals[0] = 10;
		cons_vals[1] = 15;
		//cons_vals[2] = 240;

		int mat[][] = new int[num_cons][num_vars + num_cons + 1];
		for (int i=0; i<num_cons; ++i)
			mat[i][num_vars+i] = 1;

		mat[0][0] = 3;
		mat[0][1] = 2;
		mat[0][2] = 1;

		mat[1][0] = 2;
		mat[1][1] = 5;
		mat[1][2] = 3;

		/*mat[2][0] = 2;
		mat[2][1] = 1;
		mat[2][2] = 2;*/

		Rational robj[] = new Rational[obj.length];
		for (int i=0; i<obj.length; ++i)
			robj[i] = new Rational(obj[i], 1);

		Rational rcons_vals[] = new Rational[cons_vals.length];
		for (int i=0;i<cons_vals.length;++i)
			rcons_vals[i] = new Rational(cons_vals[i], 1);

		Rational rmat[][] = new Rational[mat.length][mat[0].length];
		for (int r=0; r < mat.length; ++r)
			for (int c=0; c < mat[r].length; ++c)
				rmat[r][c] = new Rational(mat[r][c], 1);

		simplex(robj, rcons_vals, rmat);
		display(robj, rcons_vals, rmat);
	}
}
