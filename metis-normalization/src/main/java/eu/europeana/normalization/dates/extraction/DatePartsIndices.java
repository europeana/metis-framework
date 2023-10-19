package eu.europeana.normalization.dates.extraction;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Enum containing triples of the group indices.
 * <p>The positions are Left = Year, Middle = Month, Right = Day</p>
 */
public enum DatePartsIndices {
  DMY_INDICES(ImmutableTriple.of(3, 2, 1)),
  YMD_INDICES(ImmutableTriple.of(1, 2, 3)),
  MDY_INDICES(ImmutableTriple.of(3, 1, 2)),
  MY_INDICES(ImmutableTriple.of(2, 1, null));

  private final Triple<Integer, Integer, Integer> indicesTriple;

  DatePartsIndices(Triple<Integer, Integer, Integer> indicesTriple) {
    this.indicesTriple = indicesTriple;
  }

  public Integer getYearIndex() {
    return indicesTriple.getLeft();
  }

  public Integer getMonthIndex() {
    return indicesTriple.getMiddle();
  }

  public Integer getDayIndex() {
    return indicesTriple.getRight();
  }
}
