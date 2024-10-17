import { GitCommitDto } from "../../api";

export interface ExtendedGitCommitDto extends GitCommitDto {
  parent: string | undefined;
}
