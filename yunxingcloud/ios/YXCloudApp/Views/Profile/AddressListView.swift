import SwiftUI

/// 收货地址管理列表
struct AddressListView: View {
    @EnvironmentObject private var addressStore: AddressStore
    @State private var editingAddress: UserAddress?
    @State private var showAddSheet = false

    var body: some View {
        Group {
            if addressStore.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = addressStore.errorMessage {
                VStack(spacing: 12) {
                    Text(error).foregroundStyle(.secondary)
                    Button("重试") {
                        Task { await addressStore.load() }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(AppConfig.brandRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if addressStore.addresses.isEmpty {
                EmptyStateView(icon: "mappin.and.ellipse", title: "还没有收货地址", subtitle: "点击右上角添加")
            } else {
                addressList
            }
        }
        .background(AppConfig.pageBackground)
        .navigationTitle("收货地址")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    showAddSheet = true
                } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .task { await addressStore.load() }
        .refreshable { await addressStore.load() }
        .sheet(isPresented: $showAddSheet) {
            NavigationStack { AddressEditView(address: nil) }
        }
        .sheet(item: $editingAddress) { address in
            NavigationStack { AddressEditView(address: address) }
        }
    }

    private var addressList: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(addressStore.addresses) { address in
                    addressCard(address)
                        .onTapGesture { editingAddress = address }
                }
            }
            .padding(10)
        }
    }

    private func addressCard(_ address: UserAddress) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(address.name)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text(address.phone)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                if address.isDefault == true {
                    Text("默认")
                        .font(.caption2)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(AppConfig.brandRed.opacity(0.1))
                        .foregroundStyle(AppConfig.brandRed)
                        .clipShape(Capsule())
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }
            Text(address.fullAddress)
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding(12)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .swipeActions {
            Button("删除", role: .destructive) {
                Task { await addressStore.delete(address) }
            }
        }
    }
}

/// 新增/编辑地址表单
struct AddressEditView: View {
    @EnvironmentObject private var addressStore: AddressStore
    @Environment(\.dismiss) private var dismiss

    let address: UserAddress?

    @State private var name = ""
    @State private var phone = ""
    @State private var province = ""
    @State private var city = ""
    @State private var district = ""
    @State private var detail = ""
    @State private var isDefault = false
    @State private var isSaving = false
    @State private var errorMessage: String?

    private var isEditMode: Bool { address != nil }

    var body: some View {
        Form {
            Section("收货人（必填）") {
                TextField("姓名", text: $name)
                TextField("手机号", text: $phone)
                    .keyboardType(.phonePad)
            }

            Section("所在地区") {
                TextField("省份", text: $province)
                TextField("城市", text: $city)
                TextField("区/县", text: $district)
            }

            Section("详细地址") {
                TextField("街道、门牌号等", text: $detail, axis: .vertical)
                    .lineLimit(2...4)
            }

            Section {
                Toggle("设为默认地址", isOn: $isDefault)
            }

            Section {
                Button {
                    Task { await save() }
                } label: {
                    HStack {
                        Spacer()
                        if isSaving {
                            ProgressView().tint(.white)
                        } else {
                            Text(isEditMode ? "保存修改" : "添加地址").fontWeight(.semibold)
                        }
                        Spacer()
                    }
                }
                .disabled(isSaving || name.isEmpty || phone.isEmpty)
                .listRowBackground(AppConfig.brandRed)
                .foregroundStyle(.white)
            } footer: {
                if let errorMessage {
                    Text(errorMessage).foregroundStyle(AppConfig.brandRed)
                }
            }

            if isEditMode {
                Section {
                    Button("删除此地址", role: .destructive) {
                        Task {
                            await addressStore.delete(address!)
                            dismiss()
                        }
                    }
                }
            }
        }
        .navigationTitle(isEditMode ? "编辑地址" : "添加地址")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("关闭") { dismiss() }
            }
        }
        .onAppear {
            guard let address else { return }
            name = address.name
            phone = address.phone
            province = address.province ?? ""
            city = address.city ?? ""
            district = address.district ?? ""
            detail = address.detail ?? ""
            isDefault = address.isDefault ?? false
        }
    }

    private func save() async {
        isSaving = true
        errorMessage = nil
        let request = AddressRequest(
            name: name,
            phone: phone,
            province: province.isEmpty ? nil : province,
            city: city.isEmpty ? nil : city,
            district: district.isEmpty ? nil : district,
            detail: detail.isEmpty ? nil : detail,
            isDefault: isDefault
        )
        do {
            if let id = address?.id {
                try await addressStore.update(id: id, request)
            } else {
                try await addressStore.add(request)
            }
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
        isSaving = false
    }
}
