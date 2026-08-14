package hashing;

public class HashedDictionary<K, V> implements DictionaryInterface<K, V> {

    private TableEntry<K, V>[] hashTable;

    private int numberOfEntries;

    private static final int DEFAULT_SIZE = 101;

    public HashedDictionary() {
        this(DEFAULT_SIZE);
    }

    @SuppressWarnings("unchecked")
    public HashedDictionary(int tableSize) {

        hashTable =
                (TableEntry<K, V>[])
                new TableEntry[tableSize];

        numberOfEntries = 0;
    }


    // =====================================================
    // ADD
    // =====================================================

    @Override
    public V add(K key, V value) {

        if (key == null || value == null) {
            return null;
        }

        if (isFull()) {
            rehash();
        }

        int index =
                getHashIndex(key);

        int originalIndex =
                index;

        while (hashTable[index] != null) {

            if (hashTable[index].isIn()
                    && hashTable[index]
                            .getKey()
                            .equals(key)) {

                V oldValue =
                        hashTable[index]
                                .getValue();

                hashTable[index]
                        .setValue(value);

                return oldValue;
            }

            index =
                    (index + 1)
                    % hashTable.length;

            if (index == originalIndex) {
                rehash();

                return add(key, value);
            }
        }

        hashTable[index] =
                new TableEntry<K, V>(
                        key,
                        value);

        numberOfEntries++;

        return null;
    }


    // =====================================================
    // SEARCH
    // =====================================================

    @Override
    public V getValue(K key) {

        if (key == null) {
            return null;
        }

        int index =
                getHashIndex(key);

        int originalIndex =
                index;

        while (hashTable[index] != null) {

            if (hashTable[index].isIn()
                    && hashTable[index]
                            .getKey()
                            .equals(key)) {

                return hashTable[index]
                        .getValue();
            }

            index =
                    (index + 1)
                    % hashTable.length;

            if (index == originalIndex) {
                break;
            }
        }

        return null;
    }


    // =====================================================
    // REMOVE
    // =====================================================

    @Override
    public V remove(K key) {

        if (key == null) {
            return null;
        }

        int index =
                getHashIndex(key);

        int originalIndex =
                index;

        while (hashTable[index] != null) {

            if (hashTable[index].isIn()
                    && hashTable[index]
                            .getKey()
                            .equals(key)) {

                V removedValue =
                        hashTable[index]
                                .getValue();

                hashTable[index]
                        .setToRemoved();

                numberOfEntries--;

                return removedValue;
            }

            index =
                    (index + 1)
                    % hashTable.length;

            if (index == originalIndex) {
                break;
            }
        }

        return null;
    }


    // =====================================================
    // CONTAINS
    // =====================================================

    @Override
    public boolean contains(K key) {

        return getValue(key) != null;
    }


    // =====================================================
    // EMPTY
    // =====================================================

    @Override
    public boolean isEmpty() {

        return numberOfEntries == 0;
    }


    // =====================================================
    // FULL
    // =====================================================

    @Override
    public boolean isFull() {

        return numberOfEntries
                >= hashTable.length * 0.7;
    }


    // =====================================================
    // SIZE
    // =====================================================

    @Override
    public int getSize() {

        return numberOfEntries;
    }


    // =====================================================
    // CLEAR
    // =====================================================

    @Override
    public final void clear() {

        for (int i = 0;
             i < hashTable.length;
             i++) {

            hashTable[i] = null;
        }

        numberOfEntries = 0;
    }


    // =====================================================
    // HASH FUNCTION
    // =====================================================

    private int getHashIndex(K key) {

        int hashIndex =
                key.hashCode()
                % hashTable.length;

        if (hashIndex < 0) {

            hashIndex +=
                    hashTable.length;
        }

        return hashIndex;
    }


    // =====================================================
    // REHASH
    // =====================================================

    @SuppressWarnings("unchecked")
    private void rehash() {

        TableEntry<K, V>[] oldTable =
                hashTable;

        int newSize =
                getNextPrime(
                        oldTable.length * 2);

        hashTable =
                (TableEntry<K, V>[])
                new TableEntry[newSize];

        numberOfEntries = 0;

        for (int i = 0;
             i < oldTable.length;
             i++) {

            if (oldTable[i] != null
                    && oldTable[i].isIn()) {

                add(
                        oldTable[i].getKey(),
                        oldTable[i].getValue());
            }
        }
    }


    // =====================================================
    // GET ALL VALUES
    // =====================================================

    public Object[] getAllValues() {

        Object[] values =
                new Object[numberOfEntries];

        int index = 0;

        for (int i = 0;
             i < hashTable.length;
             i++) {

            if (hashTable[i] != null
                    && hashTable[i].isIn()) {

                values[index] =
                        hashTable[i]
                                .getValue();

                index++;
            }
        }

        return values;
    }


    // =====================================================
    // DISPLAY
    // =====================================================

    @Override
    public String toString() {

        String output = "";

        for (int i = 0;
             i < hashTable.length;
             i++) {

            output +=
                    String.format(
                            "%4d. ",
                            i);

            if (hashTable[i] == null) {

                output +=
                        "null\n";

            } else if (
                    hashTable[i].isRemoved()) {

                output +=
                        "removed\n";

            } else {

                output +=
                        hashTable[i]
                                .getKey()
                        + " = "
                        + hashTable[i]
                                .getValue()
                        + "\n";
            }
        }

        return output;
    }


    // =====================================================
    // PRIME NUMBER
    // =====================================================

    private int getNextPrime(int number) {

        while (!isPrime(number)) {

            number++;
        }

        return number;
    }


    private boolean isPrime(int number) {

        if (number < 2) {
            return false;
        }

        for (int i = 2;
             i * i <= number;
             i++) {

            if (number % i == 0) {
                return false;
            }
        }

        return true;
    }


    // =====================================================
    // TABLE ENTRY
    // =====================================================

    private class TableEntry<S, T> {

        private S key;

        private T value;

        private boolean inTable;


        private TableEntry(
                S searchKey,
                T dataValue) {

            key = searchKey;

            value = dataValue;

            inTable = true;
        }


        private S getKey() {

            return key;
        }


        private T getValue() {

            return value;
        }


        private void setValue(
                T newValue) {

            value = newValue;
        }


        private boolean isIn() {

            return inTable;
        }


        private boolean isRemoved() {

            return !inTable;
        }


        private void setToRemoved() {

            key = null;

            value = null;

            inTable = false;
        }
    }
}