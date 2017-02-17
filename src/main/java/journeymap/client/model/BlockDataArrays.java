package journeymap.client.model;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * A hierarchy of data that is namespaced and provides type-safe hashmaps
 * of 2-dimensional arrays (16x16).  This provides a way to store arbitrary data related
 * to specific block positions within a chunk.  One instance of a BDA can be tied to a
 * specific ChunkMD in order to store derived data.
 * <p/>
 * Namespace -> Dataset -> DataArray -> T[][]
 */
public class BlockDataArrays
{
    private HashMap<String, Dataset> datasets = new HashMap<>(8);

    public void clear()
    {
        datasets.clear();
    }

    public Dataset get(String namespace)
    {
        return datasets.computeIfAbsent(namespace, s -> new Dataset());
    }

    public static class Dataset
    {
        private DataArray<Integer> ints;
        private DataArray<Float> floats;
        private DataArray<Boolean> booleans;
        private DataArray<Object> objects;

        public void clear()
        {
            ints = null;
            floats = null;
            booleans = null;
            objects = null;
        }

        public DataArray<Integer> ints()
        {
            if (ints == null)
            {
                ints = new DataArray<>(() -> new Integer[16][16]);
            }
            return ints;
        }

        public DataArray<Float> floats()
        {
            if (floats == null)
            {
                floats = new DataArray<>(() -> new Float[16][16]);
            }
            return floats;
        }

        public DataArray<Boolean> booleans()
        {
            if (booleans == null)
            {
                booleans = new DataArray<>(() -> new Boolean[16][16]);
            }
            return booleans;
        }

        public DataArray<Object> objects()
        {
            if (objects == null)
            {
                objects = new DataArray<>(() -> new Object[16][16]);
            }
            return objects;
        }
    }

    public static class DataArray<T>
    {
        private final HashMap<String, T[][]> map = new HashMap<>(4);
        private final Supplier<T[][]> initFn;

        protected DataArray(Supplier<T[][]> initFn)
        {
            this.initFn = initFn;
        }

        public boolean has(String name)
        {
            return map.containsKey(name);
        }

        public T[][] get(String name)
        {
            return map.computeIfAbsent(name, s -> initFn.get());
        }

        public T get(String name, int x, int z)
        {
            return get(name)[z][x];
        }

        public boolean set(String name, int x, int z, T value)
        {
            T[][] arr = get(name);
            T old = arr[z][x];
            arr[z][x] = value;
            return (value != old);
        }

        public T[][] copy(String name)
        {
            T[][] src = get(name);
            T[][] dest = initFn.get();
            for (int i = 0; i < src.length; i++)
            {
                System.arraycopy(src[i], 0, dest[i], 0, src[0].length);
            }
            return dest;
        }

        public void copyTo(String srcName, String dstName)
        {
            map.put(dstName, copy(srcName));
        }

        public void clear(String name)
        {
            map.remove(name);
        }
    }

    public static boolean areIdentical(final int[][] arr, final int[][] arr2)
    {
        boolean match = true;
        for (int j = 0; j < arr.length; j++)
        {
            int[] row = arr[j];
            int[] row2 = arr2[j];
            match = IntStream.range(0, row.length).map(i -> ~row[i] | row2[i]).allMatch(n -> n == ~0);
            if (!match)
            {
                break;
            }
        }
        return match;
    }
}
