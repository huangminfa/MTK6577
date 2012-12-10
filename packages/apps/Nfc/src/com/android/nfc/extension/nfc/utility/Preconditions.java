package com.android.nfc.extension.nfc.utility;

/**
 * @hide
 */
public class Preconditions {
	private Preconditions() {}
	
	/**
	   * Ensures the truth of an expression involving one or more parameters to the
	   * calling method.
	   *
	   * @param expression a boolean expression
	   * @throws IllegalArgumentException if {@code expression} is false
	   */
	  public static void checkArgument(boolean expression) {
	    if (!expression) {
	      throw new IllegalArgumentException();
	    }
	  }

	  /**
	   * Ensures the truth of an expression involving one or more parameters to the
	   * calling method.
	   *
	   * @param expression a boolean expression
	   * @param errorMessage the exception message to use if the check fails; will
	   *     be converted to a string using {@link String#valueOf} only if needed
	   * @throws IllegalArgumentException if {@code expression} is false
	   */
	  public static void checkArgument(boolean expression, Object errorMessage) {
	    if (!expression) {
	      throw new IllegalArgumentException(String.valueOf(errorMessage));
	    }
	  }

	  /**
	   * Ensures the truth of an expression involving one or more parameters to the
	   * calling method.
	   *
	   * @param expression a boolean expression
	   * @param errorMessageFormat the {@link java.util.Formatter format string} for
	   *     the desired error message
	   * @param errorMessageArgs the arguments referenced by the format specifiers
	   *     in {@code errorMessageFormat}.
	   * @throws IllegalArgumentException if {@code expression} is false
	   * @throws NullPointerException if the check fails and either {@code
	   *     errorMessageFormat} or {@code errorMessageArgs} is null (don't let this
	   *     happen)
	   */
	  public static void checkArgument(boolean expression,
	      String errorMessageFormat, Object... errorMessageArgs) {
	    if (!expression) {
	      throw new IllegalArgumentException(
	          String.format(errorMessageFormat, errorMessageArgs));
	    }
	  }

	  /**
	   * Ensures the truth of an expression involving the state of the calling
	   * instance, but not involving any parameters to the calling method.
	   *
	   * @param expression a boolean expression
	   * @throws IllegalStateException if {@code expression} is false
	   */
	  public static void checkState(boolean expression) {
	    if (!expression) {
	      throw new IllegalStateException();
	    }
	  }

	  /**
	   * Ensures the truth of an expression involving the state of the calling
	   * instance, but not involving any parameters to the calling method.
	   *
	   * @param expression a boolean expression
	   * @param errorMessage the exception message to use if the check fails; will
	   *     be converted to a string using {@link String#valueOf} only if needed
	   * @throws IllegalStateException if {@code expression} is false
	   */
	  public static void checkState(boolean expression, Object errorMessage) {
	    if (!expression) {
	      throw new IllegalStateException(String.valueOf(errorMessage));
	    }
	  }

	  /**
	   * Ensures the truth of an expression involving the state of the calling
	   * instance, but not involving any parameters to the calling method.
	   *
	   * @param expression a boolean expression
	   * @param errorMessageFormat the {@link java.util.Formatter format string} for
	   *     the desired error message
	   * @param errorMessageArgs the arguments referenced by the format specifiers
	   *     in {@code errorMessageFormat}.
	   * @throws IllegalStateException if {@code expression} is false
	   * @throws NullPointerException if the check fails and either {@code
	   *     errorMessageFormat} or {@code errorMessageArgs} is null (don't let this
	   *     happen)
	   */
	  public static void checkState(boolean expression,
	      String errorMessageFormat, Object... errorMessageArgs) {
	    if (!expression) {
	      throw new IllegalStateException(
	          String.format(errorMessageFormat, errorMessageArgs));
	    }
	  }

	  /**
	   * Ensures that an object reference passed as a parameter to the calling
	   * method is not null, throwing a {@code NullPointerException} if it is.
	   *
	   * @param reference an object reference
	   * @return the non-null reference that was validated
	   * @throws NullPointerException if {@code reference} is null
	   */
	  public static <T> T checkNotNull(T reference) {
	    if (reference == null) {
	      throw new NullPointerException();
	    }
	    return reference;
	  }

	  /**
	   * Ensures that an object reference passed as a parameter to the calling
	   * method is not null, throwing a {@code NullPointerException} if it is.
	   *
	   * @param reference an object reference
	   * @param errorMessage the exception message to use if the check fails; will
	   *     be converted to a string using {@link String#valueOf} only if needed
	   * @return the non-null reference that was validated
	   * @throws NullPointerException if {@code reference} is null
	   */
	  public static <T> T checkNotNull(T reference, Object errorMessage) {
	    if (reference == null) {
	      throw new NullPointerException(String.valueOf(errorMessage));
	    }
	    return reference;
	  }

	  /**
	   * Ensures that an object reference passed as a parameter to the calling
	   * method is not null, throwing a {@code NullPointerException} if it is.
	   *
	   * @param reference an object reference
	   * @param errorMessageFormat the {@link java.util.Formatter format string} for
	   *     the desired error message
	   * @param errorMessageArgs the arguments referenced by the format specifiers
	   *     in {@code errorMessageFormat}.
	   * @return the non-null reference that was validated
	   * @throws NullPointerException if {@code reference} is null
	   */
	  public static <T> T checkNotNull(T reference, String errorMessageFormat,
	      Object... errorMessageArgs) {
	    if (reference == null) {
	      // If either of these parameters is null, the right thing happens anyway
	      throw new NullPointerException(
	          String.format(errorMessageFormat, errorMessageArgs));
	    }
	    return reference;
	  }

	  /**
	   * Ensures that an {@code Iterable} object passed as a parameter to the
	   * calling method is not null and contains no null elements.
	   *
	   * @param iterable any {@code Iterable} object
	   * @return the non-null {@code iterable} reference just validated
	   * @throws NullPointerException if {@code iterable} is null or contains at
	   *     least one null element
	   */
	  public static <T extends Iterable<?>> T checkContentsNotNull(T iterable) {
	    // TODO: call Iterables.containsNull()
	    for (Object element : iterable) {
	      checkNotNull(element);
	    }
	    return iterable;
	  }

	  /**
	   * Ensures that an {@code Iterable} object passed as a parameter to the
	   * calling method is not null and contains no null elements.
	   *
	   * @param iterable any {@code Iterable} object
	   * @param errorMessage the exception message to use if the check fails; will
	   *     be converted to a string using {@link String#valueOf} only if needed
	   * @return the non-null {@code iterable} reference just validated
	   * @throws NullPointerException if {@code iterable} is null or contains at
	   *     least one null element
	   */
	  public static <T extends Iterable<?>> T checkContentsNotNull(
	      T iterable, Object errorMessage) {
	    checkNotNull(iterable, errorMessage);

	    // TODO: call Iterables.containsNull()
	    for (Object element : iterable) {
	      checkNotNull(element, errorMessage);
	    }
	    return iterable;
	  }

	  /**
	   * Ensures that an {@code Iterable} object passed as a parameter to the
	   * calling method is not null and contains no null elements.
	   *
	   * @param iterable any {@code Iterable} object
	   * @param errorMessageFormat the {@link java.util.Formatter format string} for
	   *     the desired error message
	   * @param errorMessageArgs the arguments referenced by the format specifiers
	   *     in {@code errorMessageFormat}.
	   * @return the non-null {@code iterable} reference just validated
	   * @throws NullPointerException if {@code iterable} is null or contains at
	   *     least one null element
	   */
	  public static <T extends Iterable<?>> T checkContentsNotNull(T iterable,
	      String errorMessageFormat, Object... errorMessageArgs) {
	    checkNotNull(iterable, errorMessageFormat, errorMessageArgs);

	    // TODO: call Iterables.containsNull()
	    for (Object element : iterable) {
	      checkNotNull(element, errorMessageFormat, errorMessageArgs);
	    }
	    return iterable;
	  }
	 
	 
}
