# This code creates an image-recognition model, allowing you to
# choose the pretrained network model, the image, and an optional argument that
# both creates a gcore file and a file containing the memory mapping. It also
# outputs the top ten results for recognition AND top information about the process
# both before and after loading the network.
# Model to use is arg 1
# Image to process is arg 2
# Optional gcore file name is arg 3

import torch, sys, os
from torchvision import models, transforms
from PIL import Image

# Sets seed for potential randomness
torch.manual_seed(112358)

# Create a transform for the image
data_transforms = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        [0.485, 0.456, 0.406],
        [0.229, 0.224, 0.225]
    )
])

# Open the image, transform it into a valid form for the network, 
# and adds a tensor for the image
img = Image.open(sys.argv[2])
img_t = data_transforms(img)
batch_t = torch.unsqueeze(img_t, 0)

# Outputs 'top' info before network loads and evaluates the image
print("===BEFORE LOADING + EVALUATION===")
os.system('ps aux | grep "imageRecog"')

# Checks model arg, creates model and has it evaluate the image
if hasattr(models, sys.argv[1]) and callable(getattr(models, sys.argv[1])):
    model_class = getattr(models, sys.argv[1])
    model = model_class(weights='IMAGENET1K_V1')
    model.eval()

# Outputs 'top' info after network loads and evaluates the image
print("===AFTER LOADING + EVALUATION===")
os.system('ps aux | grep "imageRecog"')

# Checks if we specified a gcore filename, if so create the gcore file and
# the memory-maps file
if len(sys.argv) == 4:
    pid = os.getpid()
    catmapscmd = 'cat /proc/' + str(pid) + '/maps >> ' + str(sys.argv[3]) + '_maps.txt' #updated, 8-19-24
    err = os.system(catmapscmd) #NEW, 7-26-24
    shellcmd = 'sudo gcore -o ' + sys.argv[3] + ' ' + str(pid)
    err = os.system(shellcmd)

# Rest of code finds and outputs top 10 image recognition results
out = model(batch_t)

with open('AIresources/imagenet_classes.txt') as labels:
    classes = [i.strip() for i in labels.readlines()]

sorted, indices = torch.sort(out, descending=True)
percentage = torch.nn.functional.softmax(out, dim=1)[0] * 100.0
results = [(classes[i], percentage[i].item()) for i in indices[0][:10]]

for i in range(10):
    print('{}: {:.4f}%'.format(results[i][0], results[i][1]))
