import SwiftUI

struct RegisterView: View {
    @Environment(AuthStore.self) private var auth
    @Environment(\.dismiss) private var dismiss

    @State private var username = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var email = ""
    @State private var showPassword = false
    @State private var errorMessage: String?
    @State private var successMessage: String?

    var body: some View {
        Form {
            Section {
                TextField("用户名", text: $username)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()

                HStack {
                    if showPassword {
                        TextField("密码（至少 8 位）", text: $password)
                    } else {
                        SecureField("密码（至少 8 位）", text: $password)
                    }
                    Button {
                        showPassword.toggle()
                    } label: {
                        Image(systemName: showPassword ? "eye.slash" : "eye")
                            .foregroundStyle(.secondary)
                    }
                    .buttonStyle(.borderless)
                }

                SecureField("确认密码", text: $confirmPassword)

                TextField("邮箱（选填）", text: $email)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            } footer: {
                if let errorMessage {
                    Text(errorMessage).foregroundStyle(AppConfig.brandRed)
                }
                if let successMessage {
                    Text(successMessage).foregroundStyle(.green)
                }
            }

            Section {
                Button {
                    Task { await doRegister() }
                } label: {
                    HStack {
                        Spacer()
                        if auth.isBusy {
                            ProgressView().tint(.white)
                        } else {
                            Text("注册").fontWeight(.semibold)
                        }
                        Spacer()
                    }
                }
                .disabled(auth.isBusy || !isFormValid)
                .listRowBackground(AppConfig.brandRed)
                .foregroundStyle(.white)
            }
        }
        .navigationTitle("注册")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var isFormValid: Bool {
        !username.isEmpty && password.count >= 8 && password == confirmPassword
    }

    private func doRegister() async {
        errorMessage = nil
        successMessage = nil
        guard password == confirmPassword else {
            errorMessage = "两次输入的密码不一致"
            return
        }
        do {
            try await auth.register(
                username: username,
                password: password,
                email: email.isEmpty ? nil : email
            )
            successMessage = "注册成功，等待管理员审批后即可登录"
            // 延迟返回登录页
            try? await Task.sleep(for: .seconds(1.2))
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
