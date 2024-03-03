export class CompositeKeyMap<K, V> {

  private innerMap: Map<K, Map<K, V>> = new Map<K, Map<K, V>>();

  get(keyA: K, keyB: K): V | undefined {
    return this.innerMap.get(keyA)?.get(keyB) || this.innerMap.get(keyB)?.get(keyA);
  }

  /**
   * Super inperformant. only use for debugging
   * @param key
   */
  getAllOfKey(key: K): V[] {
    const result = new Set<V>
    const firstLevel = this.innerMap.get(key);
    if (firstLevel) {
      for (const values of firstLevel.values()) {
        result.add(values);
      }
    }
    for (const currentKey of this.innerMap.keys()) {
      if (key === currentKey) {
        continue;
      }
      const temp = this.innerMap.get(currentKey)?.get(key);
      if (temp) {
        result.add(temp);
      }
    }
    return Array.from(result);
  }

  has(keyA: K, keyB: K): boolean {
    return this.innerMap.get(keyA)?.has(keyB) || this.innerMap.get(keyB)?.has(keyA) || false;
  }

  set(keyA: K, keyB: K, value: V): this {
    if (!this.innerMap.has(keyA) && !this.innerMap.has(keyB)) {
      const innerInnerMap = new Map<K, V>;
      innerInnerMap.set(keyB, value);
      this.innerMap.set(keyA, innerInnerMap)
    } else if (this.innerMap.has(keyA)) {
      this.innerMap.get(keyA)?.set(keyB, value);
    } else if (this.innerMap.has(keyB)) {
      this.innerMap.get(keyB)?.set(keyA, value);
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
