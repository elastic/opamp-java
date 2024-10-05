package co.elastic.opamp.client.internal.request.fields.recipe;

import co.elastic.opamp.client.internal.request.fields.FieldType;
import java.util.Collection;

public final class RequestRecipe {
  private final Collection<FieldType> fields;

  public RequestRecipe(Collection<FieldType> fields) {
    this.fields = fields;
  }

  public Collection<FieldType> getFields() {
    return fields;
  }
}
