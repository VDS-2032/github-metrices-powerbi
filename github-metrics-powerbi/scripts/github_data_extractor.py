"""
GitHub Repository Metrics Extractor
====================================
This script extracts Pull Request metrics from GitHub repositories
for Power BI visualization.

Author: IKEA Development Team
Date: February 18, 2026
"""

import json
import os
import requests
import pandas as pd
from datetime import datetime, timedelta
from dateutil import parser as date_parser
from typing import List, Dict, Any, Optional


class GitHubMetricsExtractor:
    """Extracts PR metrics from GitHub repositories."""

    def __init__(self, config_path: str = "config.json"):
        """Initialize the extractor with configuration."""
        with open(config_path, 'r') as f:
            self.config = json.load(f)

        self.base_url = self.config['github']['base_url']
        self.token = self.config['github']['access_token']
        self.headers = {
            'Authorization': f'token {self.token}',
            'Accept': 'application/vnd.github.v3+json'
        }
        self.threshold_hours = self.config['thresholds']['pr_review_hours']

    def _make_request(self, endpoint: str, params: Dict = None) -> List[Dict]:
        """Make a paginated request to GitHub API."""
        all_results = []
        url = f"{self.base_url}/{endpoint}"
        params = params or {}
        params['per_page'] = 100
        page = 1

        while True:
            params['page'] = page
            response = requests.get(url, headers=self.headers, params=params)

            if response.status_code != 200:
                print(f"Error: {response.status_code} - {response.text}")
                break

            data = response.json()
            if not data:
                break

            all_results.extend(data)
            page += 1

            # Check if we've reached the last page
            if len(data) < 100:
                break

        return all_results

    def get_pull_requests(self, owner: str, repo: str, state: str = "all") -> List[Dict]:
        """Get all pull requests for a repository."""
        endpoint = f"repos/{owner}/{repo}/pulls"
        params = {'state': state, 'sort': 'created', 'direction': 'desc'}
        return self._make_request(endpoint, params)

    def get_pr_reviews(self, owner: str, repo: str, pr_number: int) -> List[Dict]:
        """Get reviews for a specific PR."""
        endpoint = f"repos/{owner}/{repo}/pulls/{pr_number}/reviews"
        return self._make_request(endpoint)

    def calculate_pr_metrics(self, owner: str, repo: str) -> pd.DataFrame:
        """Calculate PR metrics for a repository."""
        prs = self.get_pull_requests(owner, repo)
        metrics = []

        for pr in prs:
            created_at = date_parser.parse(pr['created_at'])
            merged_at = date_parser.parse(pr['merged_at']) if pr['merged_at'] else None
            closed_at = date_parser.parse(pr['closed_at']) if pr['closed_at'] else None
            now = datetime.now(created_at.tzinfo)

            # Calculate time to merge (in hours)
            time_to_merge_hours = None
            if merged_at:
                time_to_merge_hours = (merged_at - created_at).total_seconds() / 3600

            # Determine PR status
            is_merged = pr['merged_at'] is not None
            is_closed = pr['state'] == 'closed'
            is_rejected = is_closed and not is_merged
            is_open = pr['state'] == 'open'

            # Check if PR is beyond threshold
            hours_open = (now - created_at).total_seconds() / 3600
            is_beyond_threshold = is_open and hours_open > self.threshold_hours

            metrics.append({
                'repository': repo,
                'pr_number': pr['number'],
                'pr_title': pr['title'],
                'pr_url': pr['html_url'],
                'author': pr['user']['login'],
                'author_avatar': pr['user']['avatar_url'],
                'state': pr['state'],
                'is_merged': is_merged,
                'is_rejected': is_rejected,
                'is_open': is_open,
                'created_at': created_at.strftime('%Y-%m-%d %H:%M:%S'),
                'merged_at': merged_at.strftime('%Y-%m-%d %H:%M:%S') if merged_at else None,
                'closed_at': closed_at.strftime('%Y-%m-%d %H:%M:%S') if closed_at else None,
                'time_to_merge_hours': round(time_to_merge_hours, 2) if time_to_merge_hours else None,
                'hours_open': round(hours_open, 2),
                'is_beyond_threshold': is_beyond_threshold,
                'threshold_hours': self.threshold_hours,
                'labels': ','.join([label['name'] for label in pr['labels']]),
                'reviewers_requested': ','.join([r['login'] for r in pr.get('requested_reviewers', [])]),
                'base_branch': pr['base']['ref'],
                'head_branch': pr['head']['ref'],
                'additions': pr.get('additions', 0),
                'deletions': pr.get('deletions', 0),
                'changed_files': pr.get('changed_files', 0),
                'extraction_date': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            })

        return pd.DataFrame(metrics)

    def calculate_developer_aggregates(self, pr_df: pd.DataFrame) -> pd.DataFrame:
        """Calculate aggregate metrics per developer."""
        if pr_df.empty:
            return pd.DataFrame()

        developer_metrics = pr_df.groupby(['repository', 'author']).agg({
            'pr_number': 'count',
            'is_merged': 'sum',
            'is_rejected': 'sum',
            'is_open': 'sum',
            'time_to_merge_hours': 'mean',
            'is_beyond_threshold': 'sum'
        }).reset_index()

        developer_metrics.columns = [
            'repository',
            'developer',
            'total_prs',
            'merged_prs',
            'rejected_prs',
            'open_prs',
            'avg_time_to_merge_hours',
            'prs_beyond_threshold'
        ]

        developer_metrics['merge_rate'] = (
            developer_metrics['merged_prs'] / developer_metrics['total_prs'] * 100
        ).round(2)

        developer_metrics['rejection_rate'] = (
            developer_metrics['rejected_prs'] / developer_metrics['total_prs'] * 100
        ).round(2)

        developer_metrics['extraction_date'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

        return developer_metrics

    def calculate_repository_aggregates(self, pr_df: pd.DataFrame) -> pd.DataFrame:
        """Calculate aggregate metrics per repository."""
        if pr_df.empty:
            return pd.DataFrame()

        repo_metrics = pr_df.groupby('repository').agg({
            'pr_number': 'count',
            'is_merged': 'sum',
            'is_rejected': 'sum',
            'is_open': 'sum',
            'time_to_merge_hours': 'mean',
            'is_beyond_threshold': 'sum',
            'author': 'nunique'
        }).reset_index()

        repo_metrics.columns = [
            'repository',
            'total_prs',
            'merged_prs',
            'rejected_prs',
            'open_prs',
            'avg_time_to_merge_hours',
            'prs_beyond_threshold',
            'unique_contributors'
        ]

        repo_metrics['merge_rate'] = (
            repo_metrics['merged_prs'] / repo_metrics['total_prs'] * 100
        ).round(2)

        repo_metrics['rejection_rate'] = (
            repo_metrics['rejected_prs'] / repo_metrics['total_prs'] * 100
        ).round(2)

        repo_metrics['extraction_date'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

        return repo_metrics

    def calculate_overall_aggregates(self, pr_df: pd.DataFrame) -> pd.DataFrame:
        """Calculate overall aggregate metrics across all repositories."""
        if pr_df.empty:
            return pd.DataFrame()

        overall = {
            'total_repositories': pr_df['repository'].nunique(),
            'total_prs': len(pr_df),
            'total_merged': pr_df['is_merged'].sum(),
            'total_rejected': pr_df['is_rejected'].sum(),
            'total_open': pr_df['is_open'].sum(),
            'avg_time_to_merge_hours': round(pr_df['time_to_merge_hours'].mean(), 2),
            'total_beyond_threshold': pr_df['is_beyond_threshold'].sum(),
            'unique_contributors': pr_df['author'].nunique(),
            'overall_merge_rate': round(pr_df['is_merged'].sum() / len(pr_df) * 100, 2),
            'overall_rejection_rate': round(pr_df['is_rejected'].sum() / len(pr_df) * 100, 2),
            'extraction_date': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        }

        return pd.DataFrame([overall])

    def get_threshold_violations(self, pr_df: pd.DataFrame) -> pd.DataFrame:
        """Get all PRs that are beyond the threshold."""
        violations = pr_df[pr_df['is_beyond_threshold'] == True].copy()
        violations['hours_overdue'] = violations['hours_open'] - self.threshold_hours
        return violations

    def extract_all_metrics(self) -> Dict[str, pd.DataFrame]:
        """Extract all metrics for all configured repositories."""
        all_prs = []

        for repo_config in self.config['repositories']:
            owner = repo_config['owner']
            repo = repo_config['name']
            print(f"Extracting metrics for {owner}/{repo}...")

            try:
                pr_df = self.calculate_pr_metrics(owner, repo)
                all_prs.append(pr_df)
                print(f"  Found {len(pr_df)} pull requests")
            except Exception as e:
                print(f"  Error extracting {repo}: {e}")

        if not all_prs:
            return {}

        # Combine all PR data
        combined_prs = pd.concat(all_prs, ignore_index=True)

        return {
            'pull_requests': combined_prs,
            'developer_aggregates': self.calculate_developer_aggregates(combined_prs),
            'repository_aggregates': self.calculate_repository_aggregates(combined_prs),
            'overall_aggregates': self.calculate_overall_aggregates(combined_prs),
            'threshold_violations': self.get_threshold_violations(combined_prs)
        }

    def save_to_csv(self, metrics: Dict[str, pd.DataFrame], output_dir: str = None):
        """Save all metrics to CSV files."""
        output_dir = output_dir or self.config['output']['data_directory']
        os.makedirs(output_dir, exist_ok=True)

        for name, df in metrics.items():
            filepath = os.path.join(output_dir, f"{name}.csv")
            df.to_csv(filepath, index=False)
            print(f"Saved {name} to {filepath}")


def main():
    """Main entry point."""
    print("=" * 60)
    print("GitHub Repository Metrics Extractor")
    print("=" * 60)
    print()

    extractor = GitHubMetricsExtractor()
    metrics = extractor.extract_all_metrics()

    if metrics:
        extractor.save_to_csv(metrics)
        print()
        print("=" * 60)
        print("Extraction Complete!")
        print("=" * 60)
        print()
        print("Summary:")
        print(f"  Total PRs: {len(metrics['pull_requests'])}")
        print(f"  Repositories: {metrics['overall_aggregates']['total_repositories'].values[0]}")
        print(f"  Contributors: {metrics['overall_aggregates']['unique_contributors'].values[0]}")
        print(f"  PRs Beyond Threshold: {metrics['overall_aggregates']['total_beyond_threshold'].values[0]}")
    else:
        print("No metrics extracted. Check your configuration and access token.")


if __name__ == "__main__":
    main()

