export class CompositeKeyMap<K, V> {

  private innerMap: Map<K, Map<K, V>> = new Map<K, Map<K, V>>();

  get(keyA: K, keyB: K): V | undefined {
    return this.innerMap.get(keyA)?.get(keyB);
  }

  getAllOfKey(key: K): V[] {
    const result = new Set<V>
    const firstLevel = this.innerMap.get(key);
    if (firstLevel) {
      for (const values of firstLevel.values()) {
        result.add(values);
      }
    }
    return Array.from(result);
  }

  has(keyA: K, keyB: K): boolean {
    return this.innerMap.get(keyA)?.has(keyB) || false;
  }

  set(keyA: K, keyB: K, value: V): this {
    if (!this.innerMap.has(keyA)) {
      const innerInnerMap = new Map<K, V>;
      innerInnerMap.set(keyB, value);
      this.innerMap.set(keyA, innerInnerMap)
    } else if (this.innerMap.has(keyA)) {
      this.innerMap.get(keyA)?.set(keyB, value);
    }
    return this;
  }

  size(): number {
    let count = 0;
    for (const innerInnerMap of this.innerMap.values()) {
      count += innerInnerMap.size;
    }
    return count;
  }
}
