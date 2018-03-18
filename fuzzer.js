const child_process = require('child_process')
const fs = require('fs')
const path = require('path')

var http = require('http');

const getFileList = (dir, fileList = []) => {
    fs.readdirSync(dir).forEach(file => {
        fileList = fs.statSync(path.join(dir, file)).isDirectory()
            ? getFileList(path.join(dir, file), fileList)
            : fileList.concat(path.join(dir, file));
    });
    return fileList;
}

const getJavaFiles = (dirPath)=>{
    let filePaths = getFileList(dirPath)
    let javaFiles = []

    filePaths.forEach(file => {
        if (!file.match(/sql/) && !file.match(/model/) && path.basename(file).match(/[a-zA-Z0-9._/]+[.]java$/g)) {
            javaFiles.push(file)
        }
    })
    console.log(javaFiles);
    return javaFiles;
}


const fileFuzzer = (filePath) => {
    let linesinFile = fs.readFileSync(filePath, 'utf8').split(/\r?\n/)
    fs.writeFileSync(filePath, '', {encoding:'utf8'});

    linesinFile.forEach(line=>{
        let rnd = Math.random();
        let desiredFreq = 1;
        let freq = 1 - desiredFreq;

        if(rnd > freq && !line.match(/@/) && !line.match(/\\/))
            line = line.replace(/(\"[\w\s]+\")/g, '"sampletext"')
            //line = line.replace(/"([^"strings"]*)"/g, '"sampletext"')


        rnd = Math.random()

        if(rnd > freq && !line.match(/<.+>/) && (line.match(/while/) || line.match(/if/)))
            line = line.replace('<', '>')
        else if(rnd < freq && !line.match(/<.+>/) && (line.match(/while/) || line.match(/if/)))
            line = line.replace('>', '<')

        rnd = Math.random()

        if(rnd > freq)
            line = line.replace('==', '!=')
        else
            line = line.replace('!=', '==')       
    
        if(line != '\r')
            line += '\n'

        fs.appendFileSync(filePath, line, {encoding:'utf8'});
    })
}

const commitFuzzer = (master_sha1, n) => {
    child_process.execSync(`git stash && git checkout fuzzer && git checkout stash -- . && git commit -am "Commit Number ${n}: Fuzzing master:${master_sha1}" && git push`)
    child_process.execSync('git stash drop');
    let lastCommitSha1 = child_process.execSync(`git rev-parse fuzzer`).toString().trim()
    return lastCommitSha1;
}

const rollbackAndResetCommit = (firstCommitSha1) => {
    child_process.execSync('git checkout ${firstCommitSha1}')
}

const triggerBuild = (githubURL, jenkinsIP, jenkinsToken, lastCommitsha1) => {
    try {
        child_process.execSync('curl "http://' + jenkinsIP + ':8080/git/notifyCommit?url=' + githubURL + '&branches=fuzzer"')
        console.log('Fuzzer number ${lastCommitsha1} - Succesfully triggered build.')
    } catch (error) {
        console.log('Fuzzer number ${lastCommitsha1} - Could not trigger build.')
    }
}

const mainForFuzzing = (n) => {
    let master_sha1 = process.env.MASTER_SHA1;
    let sha1 = process.env.SHA1;
    let jenkinsIP = process.env.JENKINS_IP;
    let jenkinsToken = process.env.JENKINS_BUILD_TOKEN;
    let githubURL = process.env.GITHUB_URL

    for (var i = 0; i < n; i++) {
        let javaFiles = getJavaFiles(__dirname + '/iTrust2/src/main/java/edu/ncsu/csc/itrust2');
        rollbackAndResetCommit(sha1)
        javaFiles.forEach(javaFile =>{
            let rnd = Math.random();
            let desiredFreq = 1;
            let freq = 1 - desiredFreq;

            if(rnd > 0)
                fileFuzzer(javaFile);
        })
        let lastCommitSha1 = commitFuzzer(master_sha1, i);
        //triggerBuild(githubURL, jenkinsIP, jenkinsToken, lastCommitSha1)
    }
}

mainForFuzzing(5);
