package ch.aaap.prototype.platform.web;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** The one list shape every list endpoint returns. Pages are zero-based. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems) {

  public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
    return new PageResponse<>(
        page.map(mapper).getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
  }
}
