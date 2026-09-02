import SwiftUI

/// 分类页：左侧分类栏 + 右侧商品网格
struct CategoryView: View {
    @EnvironmentObject private var catalog: CatalogStore
    @State private var selectedCategory: Category?
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            HStack(spacing: 0) {
                categoryList
                    .frame(width: 100)
                    .background(Color(.systemGray6))

                productArea
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(AppConfig.pageBackground)
            }
            .navigationTitle("分类")
            .navigationBarTitleDisplayMode(.inline)
            .navigationDestination(for: Route.self) { route in
                switch route {
                case .productDetail(let id):
                    ProductDetailView(productId: id)
                case .products(let title, let categoryId):
                    ProductListView(title: title, categoryId: categoryId)
                case .search:
                    SearchView()
                case .login:
                    LoginView()
                case .register:
                    RegisterView()
                }
            }
        }
        .task {
            if catalog.categories.isEmpty { await catalog.loadCategories() }
            if catalog.products.isEmpty { await catalog.loadProducts(categoryId: selectedCategory?.id) }
        }
    }

    private var categoryList: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(catalog.topLevelCategories) { cat in
                    Button {
                        selectedCategory = cat
                        Task { await catalog.loadProducts(categoryId: cat.id) }
                    } label: {
                        Text(cat.name)
                            .font(.system(size: 13))
                            .foregroundStyle(selectedCategory?.id == cat.id ? AppConfig.brandRed : .primary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(selectedCategory?.id == cat.id ? Color.white : Color.clear)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    private var productArea: some View {
        Group {
            if catalog.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = catalog.errorMessage {
                VStack(spacing: 12) {
                    Text(error).foregroundStyle(.secondary)
                    Button("重试") {
                        Task { await catalog.loadProducts(categoryId: selectedCategory?.id) }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(AppConfig.brandRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if catalog.products.isEmpty {
                Text("该分类暂无商品")
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                productGrid
            }
        }
    }

    private var productGrid: some View {
        ScrollView {
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 2), spacing: 12) {
                ForEach(catalog.products) { product in
                    Button {
                        path.append(Route.productDetail(product.id))
                    } label: {
                        ProductCard(product: product)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(10)
        }
    }
}
