package common;

public class Triplet<A, B, C> {

	public A a;
	public B b;
	public C c;

	public Triplet(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (this == obj) {
			return true;
		} else if (!getClass().equals(obj.getClass())) {
			return false;
		} else {
			@SuppressWarnings("rawtypes")
			Triplet other = (Triplet) obj;
			boolean aEq = a == null ? other.a == null : a.equals(other.a);
			boolean bEq = b == null ? other.b == null : b.equals(other.b);
			boolean cEq = c == null ? other.c == null : c.equals(other.c);
			return aEq && bEq && cEq;
		}
	}
	
	@Override
	public int hashCode() {
		return (a == null ? 0 : a.hashCode()) + (b == null ? 0 : b.hashCode()) + (c == null ? 0 : c.hashCode());
	}
	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append("[a=").append(a).append(", b=")
				.append(b).append(", c=").append(c).append("]").toString();
	}

}
