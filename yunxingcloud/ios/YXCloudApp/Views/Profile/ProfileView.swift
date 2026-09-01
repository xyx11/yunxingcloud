import SwiftUI

struct ProfileView: View {
    @Environment(AuthStore.self) private var auth
    @Environment(CartStore.self) private var cart

    var body: some View {
        NavigationStack {
            List {
                if auth.isLoggedIn {
                    Section {
                        HStack(spacing: 14) {
                            Image(systemName: "person.crop.circle.fill")
                                .font(.system(size: 52))
                                .foregroundStyle(AppConfig.brandRed)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(auth.currentUser?.displayName ?? auth.username ?? "用户")
                                    .font(.headline)
                                Text(auth.username ?? "")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        .padding(.vertical, 6)
                    }

                    Section {
                        LabeledContent("我的订单", value: "查看全部")
                        LabeledContent("收货地址", value: "管理")
                        LabeledContent("优惠券", value: "查看")
                    }

                    Section {
                        Button("退出登录", role: .destructive) {
                            auth.logout()
                            cart.items = []
                        }
                    }
                } else {
                    Section {
                        NavigationLink {
                            LoginView()
                        } label: {
                            HStack(spacing: 14) {
                                Image(systemName: "person.crop.circle.fill")
                                    .font(.system(size: 52))
                                    .foregroundStyle(AppConfig.brandRed)
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("登录 / 注册")
                                        .font(.headline)
                                    Text("登录后享受完整购物体验")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                            .padding(.vertical, 6)
                        }
                    }
                }

                Section {
                    LabeledContent("客服中心", value: "在线客服")
                    LabeledContent("关于我们", value: "v1.0.0")
                }
            }
            .navigationTitle("我的")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
