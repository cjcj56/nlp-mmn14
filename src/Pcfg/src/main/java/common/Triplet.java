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
			return a == null ? other.a == null
					: a.equals(other.a) && b == null ? other.b == null
							: b.equals(other.b) && c == null ? other.c == null : c.equals(other.c);
		}
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append("[a=").append(a.toString()).append(", b=")
				.append(b.toString()).append(", c=").append(c.toString()).append("]").toString();
	}

}
