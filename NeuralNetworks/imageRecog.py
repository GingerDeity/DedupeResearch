import torch, sys, os
from torchvision import models, transforms
from PIL import Image

#Model to use is arg 1
#Image to process is arg 2
#Optional gcore file name is arg 3

torch.manual_seed(112358)

data_transforms = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        [0.485, 0.456, 0.406],
        [0.229, 0.224, 0.225]
    )
])

img = Image.open(sys.argv[2])
img_t = data_transforms(img)
batch_t = torch.unsqueeze(img_t, 0)

print("===BEFORE LOADING + EVALUATION===")
os.system('ps aux | grep "imageRecog"')
if hasattr(models, sys.argv[1]) and callable(getattr(models, sys.argv[1])):
    model_class = getattr(models, sys.argv[1])
    model = model_class(weights='IMAGENET1K_V1')
    model.eval()
print("===AFTER LOADING + EVALUATION===")
os.system('ps aux | grep "imageRecog"')

if len(sys.argv) == 4:
    pid = os.getpid()
    catmapscmd = 'cat /proc/' + str(pid) + '/maps >> ' + str(sys.argv[3]) + '_maps.txt' #updated, 8-19-24
    err = os.system(catmapscmd) #NEW, 7-26-24
    shellcmd = 'sudo gcore -o ' + sys.argv[3] + ' ' + str(pid)
    err = os.system(shellcmd)

out = model(batch_t)

with open('AIresources/imagenet_classes.txt') as labels:
    classes = [i.strip() for i in labels.readlines()]

sorted, indices = torch.sort(out, descending=True)
percentage = torch.nn.functional.softmax(out, dim=1)[0] * 100.0
results = [(classes[i], percentage[i].item()) for i in indices[0][:10]]

for i in range(10):
    print('{}: {:.4f}%'.format(results[i][0], results[i][1]))
