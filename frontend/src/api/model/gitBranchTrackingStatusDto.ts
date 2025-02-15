/**
 * OpenAPI definition
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: v0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

export interface GitBranchTrackingStatusDto {
  id?: string;
  branchAId?: string;
  branchBId?: string;
  mergeBaseCommitId?: string;
  mergeStatus?: GitBranchTrackingStatusDto.MergeStatusEnum;
  aheadCount?: number;
  behindCount?: number;
  conflictingFilePaths?: Array<string>;
}

export namespace GitBranchTrackingStatusDto {
  export type MergeStatusEnum =
          'MERGED'
          | 'TWO_WAY_MERGEABLE'
          | 'THREE_WAY_MERGEABLE'
          | 'CONFLICTS'
          | 'UNKNOWN_MERGE_BASE';
  export const MergeStatusEnum = {
    Merged: 'MERGED' as MergeStatusEnum,
    TwoWayMergeable: 'TWO_WAY_MERGEABLE' as MergeStatusEnum,
    ThreeWayMergeable: 'THREE_WAY_MERGEABLE' as MergeStatusEnum,
    Conflicts: 'CONFLICTS' as MergeStatusEnum,
    UnknownMergeBase: 'UNKNOWN_MERGE_BASE' as MergeStatusEnum
  };
}


