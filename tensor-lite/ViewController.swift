//
//  ViewController.swift
//  tensor-lite
//
//  Created by Hardik Hardiya on 11/1/18.
//  Copyright © 2018 Hardik Hardiya. All rights reserved.
//Reference->
//https://www.raywenderlich.com/5653-create-ml-tutorial-getting-started?fbclid=IwAR1L_yWUmceRcUR50PhfPtTlLyJ0v11AA3i96itRDE9vy3zyBqZqdZ1szLg
//https://www.raywenderlich.com/577-core-ml-and-vision-machine-learning-in-ios-11-tutorial?fbclid=IwAR1uxU6rQtclEY7dnNAsTEafkifoECk0gRYtR_xNYltdiUf9CzT6GcTKFIw
//https://developer.apple.com/machine-learning/build-run-models/?fbclid=IwAR1j-GmgUqAMD8DAd5sv_RR2OnMXNd7rj_kzSCJLC0kFLAlkZ-aDsXE_QqM
//https://www.youtube.com/watch?v=tzYXjygTEgs
//http://places2.csail.mit.edu/
//Authors -> B. Zhou, A. Lapedriza, J. Xiao, A. Torralba, and A. Oliva

import CoreML
import Vision
import UIKit

class ViewController: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate {

   
    @IBOutlet weak var imagePick: UIImageView!
    @IBOutlet weak var answerLabel: UILabel!
    let vowels: [Character] = ["a", "e", "i", "o", "u"]
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    @IBAction func chooseImage(_ sender: Any) {
        
        let imagePickerController = UIImagePickerController()
        imagePickerController.delegate = self
        
        let actionSheet = UIAlertController(title: "Photo Source", message: "Choose a source", preferredStyle: .actionSheet)
        actionSheet.addAction(UIAlertAction(title: "Camera", style: .default, handler: {(action:UIAlertAction)in
            if UIImagePickerController.isSourceTypeAvailable(.camera){
                imagePickerController.sourceType = .camera
                self.present(imagePickerController, animated: true, completion: nil)}
            else{
                print("Camera not available")
            }
        }))
        
        actionSheet.addAction(UIAlertAction(title: "Photo Library", style: .default, handler: {(action:UIAlertAction)in
            imagePickerController.sourceType = .photoLibrary
            self.present(imagePickerController, animated: true, completion: nil)
        }))
        
        actionSheet.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        self.present(actionSheet, animated: true, completion: nil)
    }
    
    // MARK: - Methods
   
        
        func detectScene(image: CIImage) {
            answerLabel.text = "detecting scene..."
            
            // Load the ML model through its generated class
            guard let model = try? VNCoreMLModel(for: GoogLeNetPlaces().model) else {
                fatalError("can't load Places ML model")
            }
            let request = VNCoreMLRequest(model: model) { [weak self] request, error in
                guard let results = request.results as? [VNClassificationObservation],
                    let topResult = results.first else {
                        fatalError("unexpected result type from VNCoreMLRequest")
                }
                
                // Update UI on main queue
                let article = (self?.vowels.contains(topResult.identifier.first!))! ? "an" : "a"
                DispatchQueue.main.async { [weak self] in
                    self?.answerLabel.text = "\(Int(topResult.confidence * 100))% it's \(article) \(topResult.identifier)"
                }
            }
            // Run the Core ML GoogLeNetPlaces classifier on global dispatch queue
            let handler = VNImageRequestHandler(ciImage: image)
            DispatchQueue.global(qos: .userInteractive).async {
                do {
                    try handler.perform([request])
                } catch {
                    print(error)
                }
            }
            
        }
    
 
    
     func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        
        guard let image = info[.originalImage] as? UIImage else {
            fatalError("Expected a dictionary containing an image, but was provided the following: \(info)")
        }
        
        imagePick.image = image
        
        picker.dismiss(animated: true, completion: nil)
        guard let ciImage = CIImage(image: image) else {
            fatalError("couldn't convert UIImage to CIImage")
        }
        
        detectScene(image: ciImage)
    }
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
}

