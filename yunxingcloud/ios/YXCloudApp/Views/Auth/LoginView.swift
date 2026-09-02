import SwiftUI

struct LoginView: View {
    @EnvironmentObject private var auth: AuthStore
    @Environment(\.dismiss) private var dismiss

    @State private var username = ""
    @State private var password = ""
    @State private var showPassword = false
    @State private var errorMessage: String?

    var body: some View {
        Form {
            Section {
                TextField("用户名", text: $username)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()

                HStack {
                    if showPassword {
                        TextField("密码", text: $password)
                            .submitLabel(.go)
                            .onSubmit { Task { await doLogin() } }
                    } else {
                        SecureField("密码", text: $password)
                            .submitLabel(.go)
                            .onSubmit { Task { await doLogin() } }
                    }
                    Button {
                        showPassword.toggle()
                    } label: {
                        Image(systemName: showPassword ? "eye.slash" : "eye")
                            .foregroundStyle(.secondary)
                    }
                    .buttonStyle(.borderless)
                }
            } footer: {
                if let errorMessage {
                    Text(errorMessage).foregroundStyle(AppConfig.brandRed)
                }
            }

            Section {
                Button {
                    Task { await doLogin() }
                } label: {
                    HStack {
                        Spacer()
                        if auth.isBusy {
                            ProgressView().tint(.white)
                        } else {
                            Text("登录").fontWeight(.semibold)
                        }
                        Spacer()
                    }
                }
                .disabled(auth.isBusy || username.isEmpty || password.isEmpty)
                .listRowBackground(AppConfig.brandRed)
                .foregroundStyle(.white)

                NavigationLink("没有账号？去注册") {
                    RegisterView()
                }
                .font(.footnote)
            }
        }
        .navigationTitle("登录")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("关闭") { dismiss() }
            }
        }
    }

    private func doLogin() async {
        errorMessage = nil
        do {
            try await auth.login(username: username, password: password)
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
