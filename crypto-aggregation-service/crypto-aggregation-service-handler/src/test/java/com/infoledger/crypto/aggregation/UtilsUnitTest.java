package com.infoledger.crypto.aggregation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.infoledger.crypto.util.Utils;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilsUnitTest {

  @Test
  void testDiff() {
    // given
    List<String> one = Arrays.asList("a", "b", "c", "d");
    List<String> two = Arrays.asList("b", "e", "d", "f");

    // when & then
    Assertions.assertEquals(Arrays.asList("a", "c"), Utils.diff(one, two));
    Assertions.assertEquals(Arrays.asList("e", "f"), Utils.diff(two, one));

    // validate one and two left unmodified
    assertEquals(Arrays.asList("a", "b", "c", "d"), one);
    assertEquals(Arrays.asList("b", "e", "d", "f"), two);
  }
}
