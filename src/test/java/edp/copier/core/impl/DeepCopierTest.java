package edp.copier.core.impl;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import edp.copier.core.impl.SimpleFieldCopier;
import edp.misc.ObjectPrinter;

public class DeepCopierTest extends junit.framework.TestCase {
    private static <T> T copy(final T t) {
        return DeepCopier.getDefault().copy(t);
    }
    private static <T> void copy(final T src, final T tgt) {
        SimpleFieldCopier.getInstance().copy(src, tgt);
    }

    /** Repeated invocation should produce a fresh copy (not a
        reference to a previous copy). */
    public void testRepeatedInvocation() {
        final DeepCopier COPIER = DeepCopier.getDefault();
        final String[] foo = new String[] { "foo" };

        final String[] ans1 = COPIER.copy(foo);
        assertTrue(ans1 != foo);
        assertEquals(1, ans1.length);
        assertEquals("foo", ans1[0]);

        final String[] ans2 = COPIER.copy(foo);
        assertTrue(ans2 != foo);
        assertTrue(ans2 != ans1);
        assertEquals(1, ans2.length);
        assertEquals("foo", ans2[0]);
    }

    public void testInteger() {
        final Integer a = 10;
        final Integer b = copy(a);
        // don't really care whether a == b or a != b here.
        assertTrue(b == 10);
    }

    public void testDistinctIntegerArray() {
        // if the original array has different instances of Integer,
        // so should the copy.
        final Integer a1 = new Integer(10);
        final Integer a2 = new Integer(10);
        final Integer[] a = new Integer[] { a1, a2 };
        final Integer[] b = copy(a);
        assertTrue(a != b);
        assertEquals(2, b.length);
        assertTrue(b[0] == 10);
        assertTrue(b[1] == 10);
        assertTrue(b[0] != a[0]);
        assertTrue(b[1] != a[1]);
        assertTrue(b[0] != b[1]);
    }

    public void testNonInternedIntegerArray() {
        // if the original array has two references to one instance of
        // a non-interned Integer, so should the copy...
        final Integer a1 = new Integer(10);
        final Integer a2 = a1;
        final Integer[] a = new Integer[] { a1, a2 };
        final Integer[] b = copy(a);
        assertTrue(a != b);
        assertEquals(2, b.length);
        assertTrue(b[0] == 10);
        assertTrue(b[0] == b[1]);
        assertTrue(b[0] != a[0]);
    }

    public void testInternedIntegerArray() {
        // if the original array has two references to one instance of
        // an interned Integer, so should the copy - I guess!?
        final Integer a1 = Integer.valueOf(10);
        final Integer a2 = a1;
        final Integer[] a = new Integer[] { a1, a2 };
        final Integer[] b = copy(a);
        assertTrue(a != b);
        assertEquals(2, b.length);
        assertTrue(b[0] == 10);
        assertTrue(b[0] == b[1]);
        assertTrue(b[0] == a[0]);
    }

    public void testArray() {
        final String[] a = new String[] { "foo", "bar" };
        final String[] b = copy(a);

        assertTrue(a != b);
        assertEquals("foo", a[0]); assertEquals("bar", a[1]);
        assertEquals("foo", b[0]); assertEquals("bar", b[1]);

        // mutate 'b'
        b[0] = "bar";
        b[1] = "foo";

        assertEquals("foo", a[0]); assertEquals("bar", a[1]);
        assertEquals("bar", b[0]); assertEquals("foo", b[1]);
    }

    public void testSelfReferentialArray() {
        final Object[] a = new Object[2]; a[0] = a; a[1] = a;
        final Object[] b = copy(a);

        assertTrue(a != b);
        assertEquals(2, a.length);
        assertEquals(2, b.length);
        assertTrue(a[0] == a); assertTrue(a[1] == a);
        assertTrue(b[0] == b); assertTrue(b[1] == b);
    }

    public void testMutuallyRecursiveArray() {
        final Object[] a = new Object[1];
        {
            final Object[] a2 = new Object[] { a };
            a[0] = a2;
        }

        assertTrue(a.length == 1);
        final Object[] a0 = (Object[]) a[0];
        assertTrue(a0.length == 1);
        final Object[] a00 = (Object[]) a0[0];
        assertTrue(a0 != a);
        assertTrue(a00 == a);

        final Object[] b = copy(a);

        assertTrue(a != b);

        // 'a' is unchanged
        assertTrue(a.length == 1);
        assertTrue(a[0] == a0);
        assertTrue(a0[0] == a);

        // 'b' is "equals" to 'a'
        assertTrue(b.length == 1);
        final Object[] b0 = (Object[]) b[0];
        assertTrue(b0.length == 1);
        final Object[] b00 = (Object[]) b0[0];
        assertTrue(b0 != b);
        assertTrue(b00 == b);

        // mutate 'b'
        b[0] = "foo";

        // 'a' is unchanged
        assertTrue(a.length == 1);
        assertTrue(a[0] == a0);
        assertTrue(a0[0] == a);
    }

    public void testArrayList() {
        final List<String> a = new ArrayList<String>(Arrays.asList("foo"));
        final List<String> b = copy(a);

        assertTrue(a != b);
        assertEquals(1, a.size());
        assertEquals(1, b.size());
        assertEquals("foo", a.get(0));
        assertEquals("foo", b.get(0));

        // mutate 'b'
        b.set(0, "bar");

        assertEquals(1, a.size());
        assertEquals(1, b.size());
        assertEquals("foo", a.get(0));
        assertEquals("bar", b.get(0));
    }

    private static class CloneableNode implements Cloneable {
        public /*mutable*/ String s;
        public /*mutable*/ CloneableNode child;
        public CloneableNode(final String s) { this(s, null); }
        public CloneableNode(final String s, final CloneableNode child) {
            this.s = s;
            this.child = child;
        }
        public String toString() {
            return child != null
                ? s + "." + child
                : s;
        }
        @SuppressWarnings("unchecked")
        public CloneableNode clone() {
            try {
                return (CloneableNode) super.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void testCloneableNode() {
        final CloneableNode a = new CloneableNode("foo", new CloneableNode("bar"));
        final CloneableNode b = copy(a);

        assertTrue(a != b);
        assertEquals("foo.bar", a.toString());
        assertEquals("foo.bar", b.toString());

        // mutate 'b'
        b.child.s = "modified";

        assertEquals("foo.bar", a.toString());
        assertEquals("foo.modified", b.toString());

        copy(a, b); // restore 'b'

        assertEquals("foo.bar", a.toString());
        assertEquals("foo.bar", b.toString());
    }

    private static class ListNode extends CloneableNode {
        public List<CloneableNode> nodes;
        public ListNode(final CloneableNode... nodes) {
            super("");
            this.nodes = Arrays.asList(nodes);
        }
        public String toString() {
            return nodes.toString();
        }
    }

    public void testListNode() {
        final ListNode a = new ListNode(new CloneableNode("foo"));
        //ObjectPrinter.DEFAULT.print(a);
        final ListNode b = copy(a);

        assertTrue(a != b);
        assertEquals("[foo]", a.toString());
        assertEquals("[foo]", b.toString());

        // mutate 'b'
        b.nodes.get(0).s = "modified";

        assertEquals("[foo]", a.toString());
        assertEquals("[modified]", b.toString());

        copy(a, b); // restore 'b'

        assertEquals("[foo]", a.toString());
        assertEquals("[foo]", b.toString());
    }

    public void testListNode2() {
        // mutates 'a' to use java.util.ArrayList.
        final ListNode a = new ListNode(new CloneableNode("foo"));
        a.nodes = new ArrayList<>();
        a.nodes.add(new CloneableNode("foo"));

        //ObjectPrinter.DEFAULT.print(a);
        final ListNode b = copy(a);

        assertTrue(a != b);
        assertEquals("[foo]", a.toString());
        assertEquals("[foo]", b.toString());

        // mutate 'b'
        b.nodes.get(0).s = "modified";

        assertEquals("[foo]", a.toString());
        assertEquals("[modified]", b.toString());

        copy(a, b); // restore 'b'

        assertEquals("[foo]", a.toString());
        assertEquals("[foo]", b.toString());
    }
}
