# PyTorch

These are some guiding instructions for conducting experiments involving neural networks.  

## Dependencies
You will need to have >= Python 3.9, and instructions for installing PyTorch can be found here: https://pytorch.org/get-started/locally/ with the options "Linux", "Pip", "Python", and "CPU" selected for my experiments.  

## Running Experiments
To run experiments using image recognition neural networks, run the command `python3 imageRecog.py $1 $2 $3` where  
$1 is the model for the neural network, of which options can be found here: https://pytorch.org/vision/stable/models.html
* Our experiments always used either 'alexnet', 'densenet121', 'resnet18', or 'googlenet'
$2 is the image to process, with choices found in the photos directory
$3 is an optional argument, allowing you to specify the filename for a core dump and memory-mapping file

For instance, obtaining a core dump of an alexnet model working on a picture of a bed would use the command `python3 imageRecog.py alexnet photos/bed.jpg bedResult`  

This also outputs `top` results both before and after loading the neural network, and also outputs the top 10 image recognitions using data from `imagenet_classes.txt`.

*Please note you may need to change the paths in imageRecog.py for it to properly work*