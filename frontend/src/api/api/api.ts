export * from './gitBranchController.service';
import { GitBranchControllerService } from './gitBranchController.service';
export * from './gitCommitController.service';
import { GitCommitControllerService } from './gitCommitController.service';
export const APIS = [GitBranchControllerService, GitCommitControllerService];
