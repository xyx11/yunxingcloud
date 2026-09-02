import SwiftUI

struct CartView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var auth: AuthStore

    @State private var showCheckout = false

    var body: some View {
        NavigationStack {
            Group {
                if !auth.isLoggedIn {
                    notLoggedInView
                } else if cart.isLoading {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if cart.items.isEmpty {
                    emptyView
                } else {
                    cartList
                }
            }
            .background(AppConfig.pageBackground)
            .navigationTitle("购物车")
            .navigationBarTitleDisplayMode(.inline)
        }
        .task {
            if auth.isLoggedIn { await cart.load() }
        }
        .onChange(of: auth.isLoggedIn) { _ in
            Task { await cart.load() }
        }
        .sheet(isPresented: $showCheckout) {
            NavigationStack { CheckoutView() }
        }
    }

    private var notLoggedInView: some View {
        VStack(spacing: 12) {
            EmptyStateView(icon: "cart", title: "登录后查看购物车", subtitle: "登录即可同步你的购物车商品")
                .frame(maxHeight: 200)
            NavigationLink {
                LoginView()
            } label: {
                Text("去登录")
                    .fontWeight(.semibold)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 32)
                    .frame(height: 44)
                    .background(AppConfig.brandRed)
                    .clipShape(Capsule())
            }
            .padding(.bottom, 40)
        }
    }

    private var emptyView: some View {
        EmptyStateView(icon: "cart", title: "购物车还是空的", subtitle: "快去挑选心仪的商品吧")
    }

    private var cartList: some View {
        VStack(spacing: 0) {
            List {
                ForEach(cart.items) { item in
                    HStack(spacing: 12) {
                        RemoteImage(url: ImageURL.resolve(item.imageUrl), cornerRadius: 8)
                            .frame(width: 72, height: 72)

                        VStack(alignment: .leading, spacing: 6) {
                            Text(item.productName ?? "商品")
                                .font(.subheadline)
                                .lineLimit(2)
                            PriceText(fen: item.priceFen, size: 15)
                        }

                        Spacer()

                        Stepper("", value: Binding(
                            get: { item.quantity },
                            set: { newQty in
                                Task { await cart.updateQuantity(item: item, quantity: newQty) }
                            }
                        ), in: 1...99)
                        .labelsHidden()
                    }
                    .swipeActions {
                        Button("删除", role: .destructive) {
                            Task { await cart.remove(item: item) }
                        }
                    }
                }
            }
            .listStyle(.plain)

            bottomBar
        }
    }

    private var bottomBar: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text("共 \(cart.totalQuantity) 件")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                PriceText(fen: cart.totalPriceFen, size: 20)
            }
            Spacer()
            Button {
                showCheckout = true
            } label: {
                Text("去结算")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 40)
                    .frame(height: 44)
                    .background(AppConfig.brandRed)
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
        .background(Color.white)
        .overlay(alignment: .top) { Divider() }
    }
}

/// 结算页：收货人表单 + 金额确认 + 提交订单
struct CheckoutView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var addressStore: AddressStore
    @Environment(\.dismiss) private var dismiss

    @State private var name = ""
    @State private var phone = ""
    @State private var address = ""
    @State private var isSubmitting = false
    @State private var errorMessage: String?
    @State private var successOrderNo: String?

    var body: some View {
        Form {
            if let defaultAddr = addressStore.defaultAddress {
                Section("默认地址") {
                    Button {
                        name = defaultAddr.name
                        phone = defaultAddr.phone
                        address = defaultAddr.fullAddress
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("\(defaultAddr.name) \(defaultAddr.phone)")
                                    .font(.subheadline)
                                    .foregroundStyle(.primary)
                                Text(defaultAddr.fullAddress)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                    .lineLimit(2)
                            }
                            Spacer()
                            Text("使用")
                                .font(.caption)
                                .foregroundStyle(AppConfig.brandRed)
                        }
                    }
                }
            }

            Section("收货信息（选填）") {
                TextField("收货人姓名", text: $name)
                TextField("手机号", text: $phone)
                    .keyboardType(.phonePad)
                TextField("收货地址", text: $address)
            }

            Section("订单金额") {
                HStack {
                    Text("共 \(cart.totalQuantity) 件商品")
                        .foregroundStyle(.secondary)
                    Spacer()
                    PriceText(fen: cart.totalPriceFen, size: 18)
                }
            }

            Section {
                Button {
                    Task { await submit() }
                } label: {
                    HStack {
                        Spacer()
                        if isSubmitting {
                            ProgressView().tint(.white)
                        } else {
                            Text("提交订单").fontWeight(.semibold)
                        }
                        Spacer()
                    }
                }
                .disabled(isSubmitting || cart.items.isEmpty)
                .listRowBackground(AppConfig.brandRed)
                .foregroundStyle(.white)
            } footer: {
                if let errorMessage {
                    Text(errorMessage).foregroundStyle(AppConfig.brandRed)
                }
                if let successOrderNo {
                    Text("下单成功！订单号：\(successOrderNo)")
                        .foregroundStyle(.green)
                }
            }
        }
        .navigationTitle("确认订单")
        .navigationBarTitleDisplayMode(.inline)
        .task { await addressStore.load() }
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("关闭") { dismiss() }
            }
        }
    }

    private func submit() async {
        isSubmitting = true
        errorMessage = nil
        do {
            let order = try await cart.submitOrder(
                name: name.isEmpty ? nil : name,
                phone: phone.isEmpty ? nil : phone,
                address: address.isEmpty ? nil : address
            )
            successOrderNo = order.orderNo
            cart.toastMessage = "下单成功：\(order.orderNo ?? "")"
            // 延迟关闭，让用户看到成功提示
            try? await Task.sleep(for: .seconds(1.5))
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
        isSubmitting = false
    }
}
