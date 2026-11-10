package com.backendlab.product_service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.backendlab.product_service.TestcontainersConfiguration;
import com.backendlab.product_service.entity.Category;
import com.backendlab.product_service.entity.Product;
import com.backendlab.product_service.repository.specification.ProductSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductRepositoryIT {

  private static final UUID CREATED_BY = UUID.fromString("66bc52f1-d1ca-4898-b6f0-05129359f5cd");

  @Autowired private ProductRepository productRepository;
  @Autowired private CategoryRepository categoryRepository;

  private Category category;
  private int skuCounter;

  @BeforeEach
  void setUp() {
    category = saveCategory("tecnologia");
  }

  private Category saveCategory(String slug) {
    return categoryRepository.save(Category.builder().name(slug).slug(slug).build());
  }

  private Product saveProduct(
      String name, BigDecimal price, boolean active, Category productCategory) {
    return productRepository.save(
        Product.builder()
            .sku("SKU-" + (++skuCounter))
            .name(name)
            .description("Motorola Moto G56 5G")
            .price(price)
            .category(productCategory)
            .active(active)
            .createdBy(CREATED_BY)
            .build());
  }

  private Product saveProduct(String name, BigDecimal price, boolean active) {
    return saveProduct(name, price, active, category);
  }

  private Product saveProduct(String name, BigDecimal price) {
    return saveProduct(name, price, true, category);
  }

  // ---------- isActive ----------

  @Test
  void shouldReturnOnlyActiveProductsWhenUsingIsActiveSpecification() {
    // given
    Product activeProduct = saveProduct("Moto G56", new BigDecimal("999.90"), true);
    saveProduct("Moto G57", new BigDecimal("1099.90"), false);

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.isActive());

    // then
    assertThat(result).extracting(Product::getId).containsExactly(activeProduct.getId());
  }

  // ---------- nameContains ----------

  @Test
  void shouldFindProductsWhenTermIsInsideTheName() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Galaxy S24", new BigDecimal("2999.00"));
    saveProduct("iPhone 15", new BigDecimal("4999.00"));

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.nameContains("oto G"));

    // then
    assertThat(result).extracting(Product::getName).containsExactly("Moto G56");
  }

  @Test
  void shouldIgnoreCaseWhenSearchingByName() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Galaxy S24", new BigDecimal("2999.00"));

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.nameContains("MOTO"));

    // then
    assertThat(result).extracting(Product::getName).containsExactly("Moto G56");
  }

  @Test
  void shouldTrimNameSearchTermBeforeMatching() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Galaxy S24", new BigDecimal("2999.00"));

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.nameContains("  moto  "));

    // then
    assertThat(result).extracting(Product::getName).containsExactly("Moto G56");
  }

  @ParameterizedTest
  @NullSource
  @EmptySource
  @ValueSource(strings = {" ", "   "})
  void shouldReturnAllProductsWhenNameFilterIsBlank(String term) {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Galaxy S24", new BigDecimal("2999.00"));
    saveProduct("iPhone 15", new BigDecimal("4999.00"));

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.nameContains(term));

    // then
    assertThat(result)
        .extracting(Product::getName)
        .containsExactlyInAnyOrder("Moto G56", "Galaxy S24", "iPhone 15");
  }

  @Test
  void shouldReturnEmptyListWhenNoProductMatchesNameFilter() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Galaxy S24", new BigDecimal("2999.00"));

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.nameContains("pixel"));

    // then
    assertThat(result).isEmpty();
  }

  // ---------- inCategory ----------

  @Test
  void shouldReturnOnlyProductsFromGivenCategoryWhenFilteringByCategory() {
    // given
    Category otherCategory = saveCategory("moda");
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Camiseta preta", new BigDecimal("59.90"), true, otherCategory);

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.inCategory(category.getId()));

    // then
    assertThat(result).extracting(Product::getName).containsExactly("Moto G56");
  }

  @Test
  void shouldReturnProductsFromAllCategoriesWhenCategoryFilterIsNull() {
    // given
    Category otherCategory = saveCategory("moda");
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Camiseta preta", new BigDecimal("59.90"), true, otherCategory);

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.inCategory(null));

    // then
    assertThat(result)
        .extracting(Product::getName)
        .containsExactlyInAnyOrder("Moto G56", "Camiseta preta");
  }

  @Test
  void shouldReturnEmptyListWhenCategoryHasNoProducts() {
    // given
    Category emptyCategory = saveCategory("vazia");
    saveProduct("Moto G56", new BigDecimal("999.90"));

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.inCategory(emptyCategory.getId()));

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyListWhenCategoryDoesNotExist() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.inCategory(UUID.randomUUID()));

    // then
    assertThat(result).isEmpty();
  }

  // ---------- priceAtLeast ----------

  @Test
  void shouldIncludeProductWithPriceEqualToMinimumWhenFilteringByMinPrice() {
    // given
    saveProduct("Abaixo", new BigDecimal("19.99"));
    saveProduct("No limite", new BigDecimal("20.00"));
    saveProduct("Acima", new BigDecimal("20.01"));

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.priceAtLeast(new BigDecimal("20.00")));

    // then
    assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("No limite", "Acima");
  }

  @Test
  void shouldNotFilterByMinPriceWhenMinPriceIsNull() {
    // given
    saveProduct("Barato", new BigDecimal("10.00"));
    saveProduct("Caro", new BigDecimal("500.00"));

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.priceAtLeast(null));

    // then
    assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("Barato", "Caro");
  }

  @Test
  void shouldReturnEmptyListWhenAllProductsAreBelowMinPrice() {
    // given
    saveProduct("Barato", new BigDecimal("10.00"));

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.priceAtLeast(new BigDecimal("50.00")));

    // then
    assertThat(result).isEmpty();
  }

  // ---------- priceAtMost ----------

  @Test
  void shouldIncludeProductWithPriceEqualToMaximumWhenFilteringByMaxPrice() {
    // given
    saveProduct("Abaixo", new BigDecimal("19.99"));
    saveProduct("No limite", new BigDecimal("20.00"));
    saveProduct("Acima", new BigDecimal("20.01"));

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.priceAtMost(new BigDecimal("20.00")));

    // then
    assertThat(result)
        .extracting(Product::getName)
        .containsExactlyInAnyOrder("Abaixo", "No limite");
  }

  @Test
  void shouldNotFilterByMaxPriceWhenMaxPriceIsNull() {
    // given
    saveProduct("Barato", new BigDecimal("10.00"));
    saveProduct("Caro", new BigDecimal("500.00"));

    // when
    List<Product> result = productRepository.findAll(ProductSpecifications.priceAtMost(null));

    // then
    assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("Barato", "Caro");
  }

  @Test
  void shouldReturnEmptyListWhenAllProductsAreAboveMaxPrice() {
    // given
    saveProduct("Caro", new BigDecimal("500.00"));

    // when
    List<Product> result =
        productRepository.findAll(ProductSpecifications.priceAtMost(new BigDecimal("50.00")));

    // then
    assertThat(result).isEmpty();
  }

  // ---------- faixa de preço (min + max) ----------

  @Test
  void shouldReturnOnlyProductsInsidePriceRangeWhenCombiningMinAndMax() {
    // given
    saveProduct("Muito barato", new BigDecimal("9.99"));
    saveProduct("Limite inferior", new BigDecimal("10.00"));
    saveProduct("Meio", new BigDecimal("50.00"));
    saveProduct("Limite superior", new BigDecimal("100.00"));
    saveProduct("Muito caro", new BigDecimal("100.01"));

    // when
    List<Product> result =
        productRepository.findAll(
            ProductSpecifications.priceAtLeast(new BigDecimal("10.00"))
                .and(ProductSpecifications.priceAtMost(new BigDecimal("100.00"))));

    // then
    assertThat(result)
        .extracting(Product::getName)
        .containsExactlyInAnyOrder("Limite inferior", "Meio", "Limite superior");
  }

  // ---------- combinação de filtros ----------

  @Test
  void shouldReturnOnlyProductsMatchingAllFiltersWhenCombiningSpecifications() {
    // given
    Category otherCategory = saveCategory("moda");
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Moto G57", new BigDecimal("999.90"), false);
    saveProduct("Moto G58", new BigDecimal("999.90"), true, otherCategory);
    saveProduct("Moto G59", new BigDecimal("5000.00"));
    saveProduct("Galaxy S24", new BigDecimal("999.90"));

    // when
    List<Product> result =
        productRepository.findAll(
            ProductSpecifications.isActive()
                .and(ProductSpecifications.nameContains("moto"))
                .and(ProductSpecifications.inCategory(category.getId()))
                .and(ProductSpecifications.priceAtLeast(new BigDecimal("500.00")))
                .and(ProductSpecifications.priceAtMost(new BigDecimal("1500.00"))));

    // then
    assertThat(result).extracting(Product::getName).containsExactly("Moto G56");
  }

  @Test
  void shouldReturnAllActiveProductsWhenAllOptionalFiltersAreNull() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));
    saveProduct("Galaxy S24", new BigDecimal("2999.00"));
    saveProduct("iPhone 15", new BigDecimal("4999.00"), false);

    // when
    List<Product> result =
        productRepository.findAll(
            ProductSpecifications.isActive()
                .and(ProductSpecifications.nameContains(null))
                .and(ProductSpecifications.inCategory(null))
                .and(ProductSpecifications.priceAtLeast(null))
                .and(ProductSpecifications.priceAtMost(null)));

    // then
    assertThat(result)
        .extracting(Product::getName)
        .containsExactlyInAnyOrder("Moto G56", "Galaxy S24");
  }

  // ---------- paginação e ordenação ----------

  @Test
  void shouldReturnFirstPageSortedByPriceAscendingWhenUsingPageable() {
    // given
    saveProduct("P30", new BigDecimal("30.00"));
    saveProduct("P10", new BigDecimal("10.00"));
    saveProduct("P50", new BigDecimal("50.00"));
    saveProduct("P20", new BigDecimal("20.00"));
    saveProduct("P40", new BigDecimal("40.00"));
    saveProduct("Inativo", new BigDecimal("1.00"), false);

    // when
    Page<Product> page =
        productRepository.findAll(
            ProductSpecifications.isActive(), PageRequest.of(0, 2, Sort.by("price")));

    // then
    assertThat(page.getContent()).extracting(Product::getName).containsExactly("P10", "P20");
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(3);
  }

  @Test
  void shouldReturnRemainingItemsOnLastPageWhenTotalIsNotMultipleOfPageSize() {
    // given
    saveProduct("P10", new BigDecimal("10.00"));
    saveProduct("P20", new BigDecimal("20.00"));
    saveProduct("P30", new BigDecimal("30.00"));
    saveProduct("P40", new BigDecimal("40.00"));
    saveProduct("P50", new BigDecimal("50.00"));

    // when
    Page<Product> page =
        productRepository.findAll(
            ProductSpecifications.isActive(), PageRequest.of(2, 2, Sort.by("price")));

    // then
    assertThat(page.getContent()).extracting(Product::getName).containsExactly("P50");
    assertThat(page.isLast()).isTrue();
  }

  @Test
  void shouldSortByPriceDescendingWhenRequested() {
    // given
    saveProduct("P10", new BigDecimal("10.00"));
    saveProduct("P30", new BigDecimal("30.00"));
    saveProduct("P20", new BigDecimal("20.00"));

    // when
    Page<Product> page =
        productRepository.findAll(
            ProductSpecifications.isActive(),
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price")));

    // then
    assertThat(page.getContent()).extracting(Product::getName).containsExactly("P30", "P20", "P10");
  }

  // ---------- métodos derivados ----------

  @Test
  void shouldReturnTrueWhenSkuAlreadyExists() {
    // given
    Product saved = saveProduct("Moto G56", new BigDecimal("999.90"));

    // when
    boolean exists = productRepository.existsBySku(saved.getSku());

    // then
    assertThat(exists).isTrue();
  }

  @Test
  void shouldReturnFalseWhenSkuDoesNotExist() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));

    // when
    boolean exists = productRepository.existsBySku("SKU-INEXISTENTE");

    // then
    assertThat(exists).isFalse();
  }

  @Test
  void shouldReturnTrueWhenCategoryHasProducts() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"));

    // when
    boolean exists = productRepository.existsByCategoryId(category.getId());

    // then
    assertThat(exists).isTrue();
  }

  @Test
  void shouldReturnTrueWhenCategoryOnlyHasInactiveProducts() {
    // given
    saveProduct("Moto G56", new BigDecimal("999.90"), false);

    // when
    boolean exists = productRepository.existsByCategoryId(category.getId());

    // then
    assertThat(exists).isTrue();
  }

  @Test
  void shouldReturnFalseWhenCategoryHasNoProducts() {
    // given
    Category emptyCategory = saveCategory("vazia");

    // when
    boolean exists = productRepository.existsByCategoryId(emptyCategory.getId());

    // then
    assertThat(exists).isFalse();
  }

  @Test
  void shouldReturnProductWhenFindingActiveProductById() {
    // given
    Product saved = saveProduct("Moto G56", new BigDecimal("999.90"));

    // when
    var result = productRepository.findByIdAndActiveTrue(saved.getId());

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  void shouldReturnEmptyWhenFindingInactiveProductById() {
    // given
    Product inactive = saveProduct("Moto G56", new BigDecimal("999.90"), false);

    // when
    var result = productRepository.findByIdAndActiveTrue(inactive.getId());

    // then
    assertThat(result).isEmpty();
  }

  // ---------- constraints do banco ----------

  @Test
  void shouldThrowDataIntegrityViolationWhenSavingProductWithDuplicatedSku() {
    // given
    Product first = saveProduct("Moto G56", new BigDecimal("999.90"));
    Product duplicated =
        Product.builder()
            .sku(first.getSku())
            .name("Outro produto")
            .price(new BigDecimal("10.00"))
            .category(category)
            .createdBy(CREATED_BY)
            .build();

    // when / then
    assertThatThrownBy(() -> productRepository.saveAndFlush(duplicated))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
