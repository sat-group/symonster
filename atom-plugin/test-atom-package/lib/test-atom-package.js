'use babel';

import TestAtomPackageView from './test-atom-package-view';
import { CompositeDisposable } from 'atom';
import FileSaver from 'file-saver'
import request from 'request'
//import java-parser from 'java-parser'

export default {

  testAtomPackageView: null,
  modalPanel: null,
  subscriptions: null,

  activate(state) {
    this.testAtomPackageView = new TestAtomPackageView(state.testAtomPackageViewState);
    this.modalPanel = atom.workspace.addModalPanel({
      item: this.testAtomPackageView.getElement(),
      visible: false
    });

    // Events subscribed to in atom's system can be easily cleaned up with a CompositeDisposable
    this.subscriptions = new CompositeDisposable();

    // Register command that toggles this view
    this.subscriptions.add(atom.commands.add('atom-workspace', {
      'test-atom-package:toggle': () => this.toggle()
    }));
  },

  deactivate() {
    this.modalPanel.destroy();
    this.subscriptions.dispose();
    this.testAtomPackageView.destroy();
  },

  serialize() {
    return {
      testAtomPackageViewState: this.testAtomPackageView.serialize()
    };
  },
  http(config) {
    return new Promise((resolve, reject ) => {
        testj = {
                  "methodName": "scale",
                  "paramNames": [
                    "sypet_arg0",
                    "sypet_arg1",
                    "sypet_arg2"
                  ],
                  "srcTypes": [
                    "java.awt.geom.Rectangle2D",
                    "double",
                    "double"
                  ],
                  "tgtType": "java.awt.geom.Rectangle2D",
                  "packages": [
                    "java.awt.geom"
                  ],
                  "testBody": "public static boolean test() throws Throwable { java.awt.geom.Rectangle2D rec = new java.awt.geom.Rectangle2D.Double(10, 20, 10, 2); java.awt.geom.Rectangle2D target = new java.awt.geom.Rectangle2D.Double(20, 60, 20, 6); java.awt.geom.Rectangle2D result = scale(rec, 2, 3); return (target.equals(result));}"
                }
        console.log(JSON.stringify(config))
        console.log(JSON.stringify(testj))
        options = {
            url: 'http://128.83.122.134:9092',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        };
        //console.log(JSON.stringify(testj))
        request.post(options, function (err, response, body) {
            if(!err && response.statusCode == 200) {
                resolve(body)
            }
            else {
                reject({
                    'error': err
                })
            }
           console.log(err, body);
       });
    })

},
toggle() {
  if (this.modalPanel.isVisible()) {
    this.modalPanel.hide();
  } else {
    const editor = atom.workspace.getActiveTextEditor();

    const words = editor.getText().split('\n');
    console.log(words)
    w = []
    for(var x =0; x < words.length ; x++) {
        if (words[x].slice(0,3) == "//#") {
            w.push(words[x].slice(3).trim())
            x++;
            w.push(words[x].slice(3).trim())
            x++;
            w.push(words[x].slice(3).trim())
            x++;
            w.push(words[x])



        }
    }
    var regex = /((?:(?:public|private|protected|static|final|abstract|synchronized|volatile)\s+)*)\s*(\w+)\s*(\w+)\((.*?)\)\s*/g
    var match = regex.exec(w[3])

    console.log(w[3])
    console.log(match[1])
    console.log(match[2])
    console.log(match[3])
    console.log(match[4])

    var regex2 = /{(.*)}/g
    var match2 = regex2.exec(w[2])

    match[4] = match[4].replace(/,/g, "" )
    l = match[4].split(" ")
    console.log(l)
    args = []
    types = []
    packages = w[0].split(" ")
    tgtType = w[1]

    //for (var i = 0; i < (libs.length); i+= 1) {
    //    libs[i] = "./lib/" + libs[i]
    //
    //}

    for (var i = 0; i+1 < (l.length); i+= 2) {
        args.push(l[i+1])
        types.push(l[i])
    }
    console.log(args)


    var config = {}
    config["methodName"] = match[3]
    config["paramNames"] = args
    config["srcTypes"] = types
    config["packages"] = packages
    config["tgtType"] = tgtType
    config["testBody"] = w[2]

    console.log(config)



    //this.testAtomPackageView.setCount(w.join("\n"));
    var blob = new Blob([w.join("\n")], {type: "text/plain;charset=utf-8"});
    console.log(w.join("\n"))
    FileSaver.saveAs(blob, "hello.txt");

    this.http(config).then((code) => {
        console.log("Returned!")
        console.log(code)
    })
    //this.modalPanel.show();
    }
}

};
