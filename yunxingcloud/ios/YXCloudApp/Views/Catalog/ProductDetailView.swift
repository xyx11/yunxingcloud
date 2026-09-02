import SwiftUI

struct ProductDetailView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var auth: AuthStore

    let productId: Int

    @State private var detail: ProductDetail?
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var quantity = 1
    @State private var showLoginSheet = false
    @State private var isAdding = false

    private let client = APIClient.shared

    var body: some View {
        Group {
            if isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let errorMessage {
                VStack(spacing: 12) {
                    Text(errorMessage).foregroundStyle(.secondary)
                    Button("重试") { Task { await load() } }
                        .buttonStyle(.borderedProminent)
                        .tint(AppConfig.brandRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let detail {
                content(detail)
            }
        }
        .navigationTitle("商品详情")
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
        .sheet(isPresented: $showLoginSheet) {
            NavigationStack { LoginView() }
        }
    }

    private func content(_ detail: ProductDetail) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                imageCarousel(detail.images)

                VStack(alignment: .leading, spacing: 8) {
                    HStack(alignment: .firstTextBaseline, spacing: 8) {
                        PriceText(fen: detail.product.price, size: 24)
                        if let sales = detail.product.sales, sales > 0 {
                            Text("已售 \(sales)")
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                        }
                    }

                    Text(detail.product.name)
                        .font(.title3)
                        .fontWeight(.semibold)

                    HStack(spacing: 8) {
                        if detail.product.isHot == true {
                            badge("热卖", color: AppConfig.brandRed)
                        }
                        if detail.product.isNew == true {
                            badge("新品", color: .green)
                        }
                        if let stock = detail.product.stock {
                            Text("库存 \(stock)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(.horizontal)

                if let desc = detail.product.description, !desc.isEmpty {
                    Divider()
                    VStack(alignment: .leading, spacing: 6) {
                        Text("商品介绍").font(.headline)
                        Text(desc)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.horizontal)
                }

                if let related = detail.related, !related.isEmpty {
                    Divider()
                    SectionHeader(title: "相关推荐")
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 2), spacing: 12) {
                        ForEach(related) { product in
                            NavigationLink(value: Route.productDetail(product.id)) {
                                ProductCard(product: product)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal)
                }
            }
            .padding(.vertical, 8)
        }
        .safeAreaInset(edge: .bottom) { bottomBar }
        .background(AppConfig.pageBackground)
    }

    private func imageCarousel(_ images: [String]) -> some View {
        Group {
            if images.isEmpty {
                RemoteImage(url: nil)
                    .frame(height: 320)
            } else {
                TabView {
                    ForEach(images, id: \.self) { img in
                        RemoteImage(url: ImageURL.resolve(img))
                            .frame(height: 320)
                    }
                }
                .tabViewStyle(.page)
                .frame(height: 320)
            }
        }
        .background(Color.white)
    }

    private func badge(_ text: String, color: Color) -> some View {
        Text(text)
            .font(.caption2)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(color.opacity(0.1))
            .foregroundStyle(color)
            .clipShape(Capsule())
    }

    private var bottomBar: some View {
        HStack(spacing: 12) {
            Stepper(value: $quantity, in: 1...99) {
                Text("数量 \(quantity)")
                    .font(.subheadline)
            }
            .fixedSize()

            Button {
                guard !isAdding else { return }
                Task {
                    isAdding = true
                    if await cart.add(productId: productId, quantity: quantity) {
                        // 成功；未登录时弹出登录
                    } else {
                        showLoginSheet = true
                    }
                    isAdding = false
                }
            } label: {
                Group {
                    if isAdding {
                        ProgressView().tint(.white)
                    } else {
                        Text("加入购物车")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                    }
                }
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .background(AppConfig.brandRed)
                .clipShape(Capsule())
            }
            .disabled(isAdding)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(.regularMaterial)
    }

    private func load() async {
        isLoading = true
        defer { isLoading = false }
        do {
            detail = try await client.request(Endpoints.productDetail(productId))
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
