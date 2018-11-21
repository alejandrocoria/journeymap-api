/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

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
    private HashMap<MapType, Dataset> datasets = new HashMap<>(8);

    /**
     * Clear all.
     */
    public void clearAll() {
        datasets.clear();
    }

    /**
     * Get dataset.
     *
     * @param mapType the map type
     * @return the dataset
     */
    public Dataset get(MapType mapType) {
        Dataset dataset = datasets.get(mapType);
        if (dataset == null) {
            dataset = new Dataset();
            datasets.put(mapType, dataset);
        }
        return dataset;
    }

    /**
     * The type Dataset.
     */
    public static class Dataset {
        private DataArray<Integer> ints;
        private DataArray<Float> floats;
        private DataArray<Boolean> booleans;
        private DataArray<Object> objects;

        Dataset() {
        }

        public Dataset(MapType mapType) {

        }

        /**
         * Clear.
         */
        protected void clear() {
            ints = null;
            floats = null;
            booleans = null;
            objects = null;
        }

        /**
         * Ints data array.
         *
         * @return the data array
         */
        public DataArray<Integer> ints() {
            if (ints == null) {
                ints = new DataArray<>(() -> new Integer[16][16]);
            }
            return ints;
        }

        /**
         * Floats data array.
         *
         * @return the data array
         */
        public DataArray<Float> floats() {
            if (floats == null) {
                floats = new DataArray<>(() -> new Float[16][16]);
            }
            return floats;
        }

        /**
         * Booleans data array.
         *
         * @return the data array
         */
        public DataArray<Boolean> booleans() {
            if (booleans == null) {
                booleans = new DataArray<>(() -> new Boolean[16][16]);
            }
            return booleans;
        }

        /**
         * Objects data array.
         *
         * @return the data array
         */
        public DataArray<Object> objects() {
            if (objects == null) {
                objects = new DataArray<>(() -> new Object[16][16]);
            }
            return objects;
        }
    }

    /**
     * The type Data array.
     *
     * @param <T> the type parameter
     */
    public static class DataArray<T> {
        private final HashMap<String, T[][]> map = new HashMap<>(4);
        private final Supplier<T[][]> initFn;

        /**
         * Instantiates a new Data array.
         *
         * @param initFn the init fn
         */
        protected DataArray(Supplier<T[][]> initFn) {
            this.initFn = initFn;
        }

        /**
         * Has boolean.
         *
         * @param name the name
         * @return the boolean
         */
        public boolean has(String name) {
            return map.containsKey(name);
        }

        /**
         * Get t [ ] [ ].
         *
         * @param name the name
         * @return the t [ ] [ ]
         */
        public T[][] get(String name) {
            return map.computeIfAbsent(name, s -> initFn.get());
        }

        /**
         * Get t.
         *
         * @param name the name
         * @param x    the x
         * @param z    the z
         * @return the t
         */
        public T get(String name, int x, int z) {
            return get(name)[z][x];
        }

        /**
         * Set boolean.
         *
         * @param name  the name
         * @param x     the x
         * @param z     the z
         * @param value the value
         * @return the boolean
         */
        public boolean set(String name, int x, int z, T value) {
            T[][] arr = get(name);
            T old = arr[z][x];
            arr[z][x] = value;
            return (value != old);
        }

        /**
         * Copy t [ ] [ ].
         *
         * @param name the name
         * @return the t [ ] [ ]
         */
        public T[][] copy(String name) {
            T[][] src = get(name);
            T[][] dest = initFn.get();
            for (int i = 0; i < src.length; i++) {
                System.arraycopy(src[i], 0, dest[i], 0, src[0].length);
            }
            return dest;
        }

        /**
         * Copy to.
         *
         * @param srcName the src name
         * @param dstName the dst name
         */
        public void copyTo(String srcName, String dstName) {
            map.put(dstName, copy(srcName));
        }

        /**
         * Clear.
         *
         * @param name the name
         */
        public void clear(String name) {
            map.remove(name);
        }
    }

    /**
     * Are identical boolean.
     *
     * @param arr  the arr
     * @param arr2 the arr 2
     * @return the boolean
     */
    public static boolean areIdentical(final int[][] arr, final int[][] arr2) {
        boolean match = true;
        for (int j = 0; j < arr.length; j++) {
            int[] row = arr[j];
            int[] row2 = arr2[j];
            match = IntStream.range(0, row.length).map(i -> ~row[i] | row2[i]).allMatch(n -> n == ~0);
            if (!match) {
                break;
            }
        }
        return match;
    }
}
