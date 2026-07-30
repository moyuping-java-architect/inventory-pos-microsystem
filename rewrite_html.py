
# 读取文件
with open(r'E:\spring boot\psi-parent\psi-cashier\src\main\resources\static\cashier.html', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# 确保文件以UTF-8编码写入，带有BOM
with open(r'E:\spring boot\psi-parent\psi-cashier\src\main\resources\static\cashier.html', 'w', encoding='utf-8-sig') as f:
    f.write(content)

print('文件已重新写入，使用UTF-8-BOM编码')