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
import { PageableObject } from './pageableObject';
import { SortObject } from './sortObject';
import { GitCommitGroupDto } from './gitCommitGroupDto';

export interface PageGitCommitGroupDto {
  totalPages?: number;
  totalElements?: number;
  first?: boolean;
  last?: boolean;
  size?: number;
  content?: Array<GitCommitGroupDto>;
  number?: number;
  sort?: SortObject;
  pageable?: PageableObject;
  numberOfElements?: number;
  empty?: boolean;
}
