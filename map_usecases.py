import os
import glob
import json

base_dir = r"c:\Users\TehoonAhn\Documents\Depth_kotlin\4th-MainProject-SeatNow-Android\app\src\main\java\com\gmg\seatnow\domain\usecase"
files = glob.glob(os.path.join(base_dir, "**", "*.kt"), recursive=True)

m = {}
for f in files:
    name = os.path.basename(f).replace('.kt', '')
    folder = os.path.basename(os.path.dirname(f))
    # assign default new mapping empty 
    m[name] = folder

print(json.dumps(m, indent=2))
