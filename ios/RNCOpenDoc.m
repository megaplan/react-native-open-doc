
#import "RNCOpenDoc.h"
#if __has_include("NSTiffSplitter.h")
#import "NSTiffSplitter.h"
#endif

@interface RNCOpenDoc ()

@property (retain, nonatomic) UIDocumentInteractionController* documentInteractionController;

@end

@implementation RNCOpenDoc

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(open: (NSURL *)path)
{
    NSString *relPath = path.relativePath;
#if __has_include("NSTiffSplitter.h")
    if ([relPath hasSuffix:@".tif"] || [relPath hasSuffix:@".tiff"]){
        NSTiffSplitter* tiffSplitter = [[NSTiffSplitter alloc] initWithImageUrl:path usingMapping:YES];
        if (tiffSplitter.countOfImages > 1) {
            [self openWith:path];
            return;
        }
    }
#endif
    self.documentInteractionController = [UIDocumentInteractionController interactionControllerWithURL:path];
    self.documentInteractionController.delegate = self;
    [self openWith:path];
}

- (void) openWith:(NSURL *)path
{
    self.documentInteractionController = [UIDocumentInteractionController interactionControllerWithURL:path];
    self.documentInteractionController.delegate = self;

    UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    while (root.presentedViewController) {
        root = root.presentedViewController;
    }
    CGRect screenRect = [[root view] bounds];
    CGFloat screenWidth = screenRect.size.width;
    CGFloat screenHeight = screenRect.size.height;
    BOOL fileOpenSuccess = [self.documentInteractionController
                            presentOptionsMenuFromRect:CGRectMake((screenWidth / 2), screenHeight, 1, 1)
                            inView:[root view] animated:YES];
    
    if (!fileOpenSuccess) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"No suitable Apps installed"
                                                        message:@"You don't seem to have any other Apps installed that can open this document."
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
    }
}

#pragma mark -
#pragma mark Document Interaction Controller Delegate Methods

- (UIViewController *) documentInteractionControllerViewControllerForPreview: (UIDocumentInteractionController *) controller
{
    return [[[[UIApplication sharedApplication] delegate] window] rootViewController];
}


@end
  
