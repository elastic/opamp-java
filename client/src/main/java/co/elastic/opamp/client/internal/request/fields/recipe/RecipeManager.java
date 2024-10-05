package co.elastic.opamp.client.internal.request.fields.recipe;

import co.elastic.opamp.client.internal.request.fields.FieldType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RecipeManager {
  private final Lock previousRecipeLock = new ReentrantLock();
  private final Lock recipeBuilderLock = new ReentrantLock();
  private List<FieldType> constantFields = new ArrayList<>();
  private RequestRecipe previousRecipe = null;
  private RecipeBuilder builder;

  public RequestRecipe previous() {
    previousRecipeLock.lock();
    try {
      return previousRecipe;
    } finally {
      previousRecipeLock.unlock();
    }
  }

  public RecipeBuilder next() {
    recipeBuilderLock.lock();
    try {
      if (builder == null) {
        builder = new RecipeBuilder(constantFields);
      }
      return builder;
    } finally {
      recipeBuilderLock.unlock();
    }
  }

  private void setPreviousRecipe(RequestRecipe recipe) {
    previousRecipeLock.lock();
    try {
      this.previousRecipe = recipe;
    } finally {
      previousRecipeLock.unlock();
    }
  }

  private void clearBuilder() {
    builder = null;
  }

  public void setConstantFields(FieldType... constantFields) {
    this.constantFields = List.of(constantFields);
  }

  public final class RecipeBuilder {
    private final Set<FieldType> fields = new HashSet<>();

    public RecipeBuilder addField(FieldType field) {
      fields.add(field);
      return this;
    }

    public RecipeBuilder addAllFields(Collection<FieldType> fields) {
      this.fields.addAll(fields);
      return this;
    }

    public RequestRecipe build() {
      recipeBuilderLock.lock();
      try {
        RequestRecipe recipe = new RequestRecipe(Collections.unmodifiableCollection(fields));
        setPreviousRecipe(recipe);
        clearBuilder();
        return recipe;
      } finally {
        recipeBuilderLock.unlock();
      }
    }

    private RecipeBuilder(List<FieldType> initialFields) {
      fields.addAll(initialFields);
    }
  }
}
