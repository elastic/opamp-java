package co.elastic.opamp.client.internal.request.fields.recipe;

import co.elastic.opamp.client.internal.request.fields.FieldType;
import java.util.List;

public final class RequestRecipe {
  private final List<FieldType> fields;

  public RequestRecipe(List<FieldType> fields) {
    this.fields = fields;
  }

  public List<FieldType> getFields() {
    return fields;
  }
}
