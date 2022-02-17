package com.infoledger.crypto.util;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

  /**
   * Checks whether the specified array is empty or not
   *
   * @param arr Array to check for emptyness
   * @return true if array is non null and contains at least one element or false otherwise.
   */
  public static boolean nonEmpty(byte[] arr) {
    return arr != null && arr.length > 0;
  }

  /**
   * Difference of list ne from list two. Only items that present in list one but not in list two.
   *
   * @param one First list.
   * @param two Second list to make difference from.
   * @param <T> Items type.
   * @return Lists difference
   */
  public static <T> List<T> diff(List<T> one, List<T> two) {
    List<T> clone = new ArrayList<>(one);
    clone.removeAll(two);
    return clone;
  }
}
