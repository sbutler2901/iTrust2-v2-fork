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
    //console.log(javaFiles);
    return javaFiles;
}


const fileFuzzer = (filePath) => {
    let linesinFile = fs.readFileSync(filePath, 'utf8').split(/\r?\n/)
    fs.writeFileSync(filePath, '', {encoding:'utf8'});

    linesinFile.forEach( line => {
        let rnd = Math.random();
        let desiredFreq = 0.5;
        let freq = 1 - desiredFreq;

        /*if( rnd > freq && !line.match(/@/) && !line.match(/\\/))
            line = line.replace(/(\"[\w\s]+\")/g, '"sampletext"');*/

        rnd = Math.random()

        /*if ( !line.match(/<.+>/) && (line.match(/while/) || line.match(/if/)) ) {
            if ( rnd > freq ) 
                line = line.replace('<', '>');
            else
                line = line.replace('>', '<');
        }*/

        rnd = Math.random()

        if ( rnd > 0 && line.match(/![a-zA-Z]/g)) {
            //console.log("line before: %s\n", line);
            line = line.replace(/![a-zA-Z]/g, (matchedString, offset, line) => {
                console.log("File %s having %s replaced", filePath, matchedString);
                return matchedString.slice(1);
            });
        }

            /*if(rnd > freq)
            line = line.replace(/==/g, '!=');
        else
            line = line.replace(/!=/g, '==');*/
    
        if(line != '\r' && line != '\n' && line != '')
            line += '\n'

        fs.appendFileSync(filePath, line, {encoding:'utf8'});
    })
}
const commitFuzzer = (master_sha1, n) => {
    child_process.execSync(`git stash && git checkout fuzzer && git checkout stash -- . && git commit -am "Commit Number ${n}: Fuzzing master:${master_sha1}" && git push --force`)
    child_process.execSync('git stash drop');
    let lastCommitSha1 = child_process.execSync(`git rev-parse fuzzer`).toString().trim()
    return lastCommitSha1;
}

const rollbackAndResetCommit = (firstCommitSha1) => {
    child_process.execSync('git checkout ${firstCommitSha1}')
}

    /*const triggerBuild = (githubURL, jenkinsIP, jenkinsToken, lastCommitsha1) => {
    try {
        child_process.execSync('curl "http://' + jenkinsIP + ':8080/git/notifyCommit?url=' + githubURL + '&branches=fuzzer"')
        console.log('Fuzzer number ${lastCommitsha1} - Succesfully triggered build.')
    } catch (error) {
        console.log('Fuzzer number ${lastCommitsha1} - Could not trigger build.')
    }
}*/

const rebase = () => {
    child_process.execSync(`git checkout fuzzer && git stash && git rebase --onto master`);
}

const getSha = () => {
    return child_process.execSync(`git rev-parse fuzzer`).toString().trim();
}

const commit = (master_sha1, n) => {
    child_process.execSync(`git add . && git commit -m "Commit Number ${n}: Fuzzing master:${master_sha1}" && git push --force`);
}

const revert = (sha1) => {
    child_process.execSync(`git revert --no-edit --no-commit ${sha1}..HEAD`);
}

const mainForFuzzing = (n) => {
    let master_sha1 = process.env.MASTER_SHA1;
    //let sha1 = process.env.SHA1;
    let jenkinsIP = process.env.JENKINS_IP;
    let jenkinsToken = process.env.JENKINS_BUILD_TOKEN;
    let githubURL = process.env.GITHUB_URL
    
    rebase();
    let sha1 = getSha();

    for (var i = 0; i < n; i++) {
        console.log("About to fuz sha %s", sha1);
        let javaFiles = getJavaFiles(__dirname + '/iTrust2/src/main/java/edu/ncsu/csc/itrust2');
        //rollbackAndResetCommit(sha1)
        //reset(master_sha1);
        javaFiles.forEach(javaFile =>{
            let rnd = Math.random();
            let desiredFreq = 1;
            let freq = 1 - desiredFreq;

            if(rnd > freq)
                fileFuzzer(javaFile);
        })
        //console.log("about to commit and push at index %d", i);
        //commit(master_sha1,i);
        //rebase();
        //revert(sha1);
        let lastCommitSha1 = commitFuzzer(master_sha1, i);
        revert(sha1);
        //rebase();
        //triggerBuild(githubURL, jenkinsIP, jenkinsToken, lastCommitSha1)
    }
}

mainForFuzzing(3);
