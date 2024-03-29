"""
Merge csv files for easy handling
"""
import sys
import os

WS_COLUMN = 1

def main(args):
    print "Merging CSV cells..."
    
    folder = args[0]
    prop_name = args[1]
    alg_name = args[2]
    
    #Filter files
    basenames = [name for name in os.listdir(folder) if is_interesting_file(name, prop_name, alg_name)]
    files = [os.path.join(folder, file) for file in basenames]
    
    if len(files) == 0:
        print "FAIL: no files found in " + folder
        return

    #Init first file: copy first column
    
    output = {}
    firstfile = files[0]
    cats = {}
    
    with open(firstfile) as f:
        lines = f.readlines()
        lines = [line.split(";") for line in lines]
        
        for idx, category in enumerate(lines[0][0:-1]):
            if idx is not WS_COLUMN:
                cats[category] = idx
        
        for field, cat in enumerate(cats):
            output[cat] = []
            for line in lines:
                output[cat].append([line[WS_COLUMN].replace(" ", "_")])
                
    print cats
                
    #Analyse all files    
    for j,file in enumerate(files):
        bmark = get_bmark_name(basenames[j], prop_name, alg_name)
        print "Handling " + bmark + " (" + basenames[j] + ")"
        
        with open(file) as f:
            lines = f.readlines()
            lines = [line.split(";") for line in lines]
            
            for i,line in enumerate(lines):
                for cat in cats:
                    field = cats[cat]
                    if i == 0:
                        output[cat][i].append(bmark)
                    else:
                        output[cat][i].append(line[field])
                    
    for cat in cats:
        output[cat] = [" ".join(line) for line in output[cat]]
        output[cat] = "\n".join(output[cat])
    
        with open(os.path.join(folder, prop_name + "-" + alg_name + "-" + cat.replace(" ", "_") + ".dat"), "w") as f:
            f.write(output[cat])
    print "DONE"
        
def is_interesting_file(basename, prop, alg):
    return basename.startswith(prop + "-") and basename.endswith("-" + alg + ".csv")

def get_bmark_name(basename, prop, alg):
    return basename[len(prop + "-"):-len("-" + alg + ".csv")]

if __name__ == "__main__":
    main(sys.argv[1:])