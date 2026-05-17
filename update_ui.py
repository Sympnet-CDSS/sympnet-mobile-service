import re

file_path = r'c:\Users\Yasmine\Documents\Symp\sympnet-mobile-service\app\src\main\res\layout\activity_edit_profile.xml'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Change label text color to teal and bold
content = re.sub(
    r'(<TextView android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"\n\s+android:layout_marginStart=\"5dp\" android:text=\"([^\"]+)\"\n\s+)android:textColor=\"#7A8A9A\" android:textSize=\"11sp\"/>',
    r'\1android:textColor=\"#009688\" android:textSize=\"13sp\" android:textStyle=\"bold\"/>',
    content
)

content = re.sub(
    r'(<TextView android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"\n\s+android:layout_marginStart=\"5dp\" android:text=\"([^\"]+)\"\n\s+)android:textColor=\"#7A8A9A\" android:textSize=\"11sp\"\/>',
    r'\1android:textColor=\"#009688\" android:textSize=\"13sp\" android:textStyle=\"bold\"\/>',
    content
)

# Alternative for different spacing
content = re.sub(
    r'android:textColor=\"#7A8A9A\" android:textSize=\"11sp\"/>',
    r'android:textColor=\"#009688\" android:textSize=\"13sp\" android:textStyle=\"bold\"/>',
    content
)

content = content.replace('app:boxBackgroundColor=\"#FFFFFF\"', 'app:boxBackgroundColor=\"#F5F6F8\"')
content = content.replace('app:boxBackgroundColor=\"#F8FAFC\"', 'app:boxBackgroundColor=\"#F5F6F8\"')
content = content.replace('app:boxCornerRadiusBottomEnd=\"9dp\"', 'app:boxCornerRadiusBottomEnd=\"12dp\"')
content = content.replace('app:boxCornerRadiusBottomStart=\"9dp\"', 'app:boxCornerRadiusBottomStart=\"12dp\"')
content = content.replace('app:boxCornerRadiusTopEnd=\"9dp\"', 'app:boxCornerRadiusTopEnd=\"12dp\"')
content = content.replace('app:boxCornerRadiusTopStart=\"9dp\"', 'app:boxCornerRadiusTopStart=\"12dp\"')
content = content.replace('app:boxStrokeColor=\"#E0EAF5\"', 'app:boxStrokeColor=\"#D1D9E6\"')
content = content.replace('app:boxStrokeColor=\"#E8EFF5\"', 'app:boxStrokeColor=\"#D1D9E6\"')
content = content.replace('app:boxStrokeWidth=\"1.5dp\"', 'app:boxStrokeWidth=\"1dp\" app:hintEnabled=\"false\"')

# Also for readonly fields that had 1dp stroke width
content = content.replace('app:boxStrokeWidth=\"1dp\">', 'app:boxStrokeWidth=\"1dp\" app:hintEnabled=\"false\">')
content = content.replace('app:boxStrokeWidth=\"1dp\" app:startIconDrawable', 'app:boxStrokeWidth=\"1dp\" app:hintEnabled=\"false\" app:startIconDrawable')

# Ensure hints are removed from TextInputLayout if hintEnabled=false does the job, we might need to remove android:hint inside TextInputEditText but actually in the image they act as placeholders, so it's fine.

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print('Updated successfully')
